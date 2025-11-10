package rv32e.core

import chisel3._
import chisel3.util._
import rv32e.bus._
import rv32e.utils.Config

/**
 * RV32E 五级流水线处理器核心
 */
class Core extends Module {
  val io = IO(new Bundle {
    // 指令存储器接口 (Wishbone Master)
    val imem = new WishboneMaster

    // 数据存储器接口 (Wishbone Master)
    val dmem = new WishboneMaster
  })

  // ============================================================
  // 模块实例化
  // ============================================================
  val regFile   = Module(new RegFile)
  val decoder   = Module(new Decoder)
  val alu       = Module(new ALU)
  val branchComp = Module(new BranchComp)

  // ============================================================
  // 流水线寄存器
  // ============================================================
  val if_id   = RegInit(0.U.asTypeOf(new IF_ID))
  val id_ex   = RegInit(0.U.asTypeOf(new ID_EX))
  val ex_mem  = RegInit(0.U.asTypeOf(new EX_MEM))
  val mem_wb  = RegInit(0.U.asTypeOf(new MEM_WB))

  // ============================================================
  // 流水线控制信号
  // ============================================================
  val stall_if = WireDefault(false.B)
  val stall_id = WireDefault(false.B)
  val flush_if = WireDefault(false.B)
  val flush_id = WireDefault(false.B)
  val flush_ex = WireDefault(false.B)

  // ============================================================
  // IF 阶段 - 取指
  // ============================================================
  val pc = RegInit(Config.RESET_VECTOR.U(32.W))
  val next_pc = WireDefault(pc + 4.U)

  // 指令存储器访问
  io.imem.adr_o := pc
  io.imem.dat_o := 0.U
  io.imem.we_o  := false.B
  io.imem.sel_o := "b1111".U
  io.imem.stb_o := !stall_if
  io.imem.cyc_o := !stall_if

  val if_inst = io.imem.dat_i
  val if_valid = io.imem.ack_i

  // IF/ID 流水线寄存器更新
  when(!stall_id && if_valid) {
    when(flush_id) {
      if_id.pc   := 0.U
      if_id.inst := 0x00000013.U  // NOP (addi x0, x0, 0)
    }.otherwise {
      if_id.pc   := pc
      if_id.inst := if_inst
    }
  }

  // ============================================================
  // ID 阶段 - 译码
  // ============================================================

  // 指令解码
  decoder.io.inst := if_id.inst
  val id_ctrl = decoder.io.ctrl

  // 提取指令字段
  val id_rs1 = Instructions.rs1(if_id.inst)
  val id_rs2 = Instructions.rs2(if_id.inst)
  val id_rd  = Instructions.rd(if_id.inst)

  // 立即数生成
  val id_imm = MuxLookup(id_ctrl.instType, 0.U)(Seq(
    InstType.I -> Instructions.immI(if_id.inst),
    InstType.S -> Instructions.immS(if_id.inst),
    InstType.B -> Instructions.immB(if_id.inst),
    InstType.U -> Instructions.immU(if_id.inst),
    InstType.J -> Instructions.immJ(if_id.inst)
  ))

  // 寄存器文件读取
  regFile.io.rs1_addr := id_rs1
  regFile.io.rs2_addr := id_rs2
  val id_rs1_data = regFile.io.rs1_data
  val id_rs2_data = regFile.io.rs2_data

  // LOAD-USE 冒险检测
  val load_use_hazard = id_ex.ctrl.memRead &&
    (id_ex.rd_addr === id_rs1 || id_ex.rd_addr === id_rs2) &&
    id_ex.rd_addr =/= 0.U

  when(load_use_hazard) {
    stall_if := true.B
    stall_id := true.B
    flush_ex := true.B
  }

  // ID/EX 流水线寄存器更新
  when(!stall_id) {
    when(flush_ex || load_use_hazard) {
      id_ex.pc       := 0.U
      id_ex.rs1_data := 0.U
      id_ex.rs2_data := 0.U
      id_ex.imm      := 0.U
      id_ex.rs1_addr := 0.U
      id_ex.rs2_addr := 0.U
      id_ex.rd_addr  := 0.U
      id_ex.ctrl.regWrite := false.B
      id_ex.ctrl.memRead  := false.B
      id_ex.ctrl.memWrite := false.B
      id_ex.ctrl.branchType := BranchType.NOBRANCH
    }.otherwise {
      id_ex.pc       := if_id.pc
      id_ex.rs1_data := id_rs1_data
      id_ex.rs2_data := id_rs2_data
      id_ex.imm      := id_imm
      id_ex.rs1_addr := id_rs1
      id_ex.rs2_addr := id_rs2
      id_ex.rd_addr  := id_rd
      id_ex.ctrl     := id_ctrl
    }
  }

  // ============================================================
  // EX 阶段 - 执行
  // ============================================================

  // 数据转发
  val forward_a = WireDefault(0.U(2.W))  // 00: no forward, 01: from MEM, 10: from WB
  val forward_b = WireDefault(0.U(2.W))

  // 转发逻辑 - 从 EX/MEM
  when(ex_mem.ctrl.regWrite && ex_mem.rd_addr =/= 0.U && ex_mem.rd_addr === id_ex.rs1_addr) {
    forward_a := 1.U
  }
  when(ex_mem.ctrl.regWrite && ex_mem.rd_addr =/= 0.U && ex_mem.rd_addr === id_ex.rs2_addr) {
    forward_b := 1.U
  }

  // 转发逻辑 - 从 MEM/WB
  when(mem_wb.ctrl.regWrite && mem_wb.rd_addr =/= 0.U &&
       mem_wb.rd_addr === id_ex.rs1_addr &&
       !(ex_mem.ctrl.regWrite && ex_mem.rd_addr =/= 0.U && ex_mem.rd_addr === id_ex.rs1_addr)) {
    forward_a := 2.U
  }
  when(mem_wb.ctrl.regWrite && mem_wb.rd_addr =/= 0.U &&
       mem_wb.rd_addr === id_ex.rs2_addr &&
       !(ex_mem.ctrl.regWrite && ex_mem.rd_addr =/= 0.U && ex_mem.rd_addr === id_ex.rs2_addr)) {
    forward_b := 2.U
  }

  // WB 数据选择
  val wb_data = MuxLookup(mem_wb.ctrl.wbSrc, mem_wb.alu_result)(Seq(
    0.U -> mem_wb.alu_result,
    1.U -> mem_wb.mem_data,
    2.U -> (mem_wb.pc + 4.U)
  ))

  // 应用转发
  val ex_rs1_data = MuxLookup(forward_a, id_ex.rs1_data)(Seq(
    0.U -> id_ex.rs1_data,
    1.U -> ex_mem.alu_result,
    2.U -> wb_data
  ))

  val ex_rs2_data = MuxLookup(forward_b, id_ex.rs2_data)(Seq(
    0.U -> id_ex.rs2_data,
    1.U -> ex_mem.alu_result,
    2.U -> wb_data
  ))

  // ALU 源操作数选择
  val alu_src1 = MuxLookup(id_ex.ctrl.aluSrc1, ex_rs1_data)(Seq(
    0.U -> ex_rs1_data,  // rs1
    1.U -> id_ex.pc,     // PC
    2.U -> 0.U           // 0
  ))

  val alu_src2 = MuxLookup(id_ex.ctrl.aluSrc2, ex_rs2_data)(Seq(
    0.U -> ex_rs2_data,  // rs2
    1.U -> id_ex.imm,    // imm
    2.U -> 4.U           // 4
  ))

  // ALU 计算
  alu.io.op   := id_ex.ctrl.aluOp
  alu.io.src1 := alu_src1
  alu.io.src2 := alu_src2
  val alu_result = alu.io.out

  // 分支判断
  branchComp.io.branchType := id_ex.ctrl.branchType
  branchComp.io.src1 := ex_rs1_data
  branchComp.io.src2 := ex_rs2_data
  val branch_taken = branchComp.io.taken

  // 分支/跳转目标地址
  val branch_target = Mux(
    id_ex.ctrl.branchType === BranchType.JALR,
    (ex_rs1_data + id_ex.imm) & "hFFFFFFFE".U,  // JALR: (rs1 + imm) & ~1
    id_ex.pc + id_ex.imm                        // 其他: PC + imm
  )

  // 分支预测错误处理
  when(branch_taken && id_ex.ctrl.branchType =/= BranchType.NOBRANCH) {
    next_pc   := branch_target
    flush_if  := true.B
    flush_id  := true.B
  }

  // EX/MEM 流水线寄存器更新
  ex_mem.pc            := id_ex.pc
  ex_mem.alu_result    := alu_result
  ex_mem.rs2_data      := ex_rs2_data
  ex_mem.rd_addr       := id_ex.rd_addr
  ex_mem.branch_target := branch_target
  ex_mem.branch_taken  := branch_taken
  ex_mem.ctrl          := id_ex.ctrl

  // ============================================================
  // MEM 阶段 - 访存
  // ============================================================

  // 数据存储器访问
  io.dmem.adr_o := ex_mem.alu_result
  io.dmem.dat_o := ex_mem.rs2_data
  io.dmem.we_o  := ex_mem.ctrl.memWrite
  io.dmem.sel_o := MuxLookup(ex_mem.ctrl.memOp, "b1111".U)(Seq(
    MemOp.SB  -> ("b0001".U << ex_mem.alu_result(1, 0)),
    MemOp.SH  -> ("b0011".U << (ex_mem.alu_result(1) << 1)),
    MemOp.SW  -> "b1111".U,
    MemOp.LB  -> ("b0001".U << ex_mem.alu_result(1, 0)),
    MemOp.LBU -> ("b0001".U << ex_mem.alu_result(1, 0)),
    MemOp.LH  -> ("b0011".U << (ex_mem.alu_result(1) << 1)),
    MemOp.LHU -> ("b0011".U << (ex_mem.alu_result(1) << 1)),
    MemOp.LW  -> "b1111".U
  ))
  io.dmem.stb_o := ex_mem.ctrl.memRead || ex_mem.ctrl.memWrite
  io.dmem.cyc_o := ex_mem.ctrl.memRead || ex_mem.ctrl.memWrite

  val mem_data_raw = io.dmem.dat_i
  val mem_valid = io.dmem.ack_i

  // LOAD 数据处理 (符号扩展/零扩展)
  val byte_offset = ex_mem.alu_result(1, 0)
  val half_offset = ex_mem.alu_result(1)

  val mem_data = MuxLookup(ex_mem.ctrl.memOp, mem_data_raw)(Seq(
    MemOp.LB  -> ((mem_data_raw >> (byte_offset << 3))(7, 0).asSInt.pad(32).asUInt),
    MemOp.LBU -> ((mem_data_raw >> (byte_offset << 3))(7, 0)),
    MemOp.LH  -> ((mem_data_raw >> (half_offset << 4))(15, 0).asSInt.pad(32).asUInt),
    MemOp.LHU -> ((mem_data_raw >> (half_offset << 4))(15, 0)),
    MemOp.LW  -> mem_data_raw
  ))

  // MEM/WB 流水线寄存器更新
  when(!ex_mem.ctrl.memRead || mem_valid) {
    mem_wb.pc         := ex_mem.pc
    mem_wb.alu_result := ex_mem.alu_result
    mem_wb.mem_data   := mem_data
    mem_wb.rd_addr    := ex_mem.rd_addr
    mem_wb.ctrl       := ex_mem.ctrl
  }.otherwise {
    // 访存时暂停流水线
    stall_if := true.B
    stall_id := true.B
  }

  // ============================================================
  // WB 阶段 - 写回
  // ============================================================

  // 寄存器文件写回
  regFile.io.wen   := mem_wb.ctrl.regWrite && mem_wb.rd_addr =/= 0.U
  regFile.io.waddr := mem_wb.rd_addr
  regFile.io.wdata := wb_data

  // ============================================================
  // PC 更新
  // ============================================================
  when(!stall_if) {
    pc := next_pc
  }
}
