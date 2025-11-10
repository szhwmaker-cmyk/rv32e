package rv32e.core

import chisel3._
import chisel3.util._

/**
 * RV32E 指令解码器
 * 解析指令并生成控制信号
 */
class Decoder extends Module {
  val io = IO(new Bundle {
    val inst = Input(UInt(32.W))
    val ctrl = Output(new ControlSignals)
  })

  import Instructions._

  // 提取指令字段
  val opcode = Instructions.opcode(io.inst)
  val funct3 = Instructions.funct3(io.inst)
  val funct7 = Instructions.funct7(io.inst)

  // 默认控制信号
  val ctrl = Wire(new ControlSignals)
  ctrl.instType   := InstType.UNKNOWN
  ctrl.aluOp      := ALUOp.XXX
  ctrl.branchType := BranchType.NOBRANCH
  ctrl.memOp      := MemOp.NOP
  ctrl.regWrite   := false.B
  ctrl.memRead    := false.B
  ctrl.memWrite   := false.B
  ctrl.aluSrc1    := 0.U  // rs1
  ctrl.aluSrc2    := 0.U  // rs2
  ctrl.wbSrc      := 0.U  // ALU

  // 根据 opcode 解码 - 使用 when/elsewhen 代替 switch
  when(opcode === "b0110111".U) {
    // LUI: rd = imm << 12
    ctrl.instType := InstType.U
    ctrl.aluOp    := ALUOp.COPY_B
    ctrl.regWrite := true.B
    ctrl.aluSrc2  := 1.U  // imm
  }.elsewhen(opcode === "b0010111".U) {
    // AUIPC: rd = PC + (imm << 12)
    ctrl.instType := InstType.U
    ctrl.aluOp    := ALUOp.ADD
    ctrl.regWrite := true.B
    ctrl.aluSrc1  := 1.U  // PC
    ctrl.aluSrc2  := 1.U  // imm
  }.elsewhen(opcode === "b1101111".U) {
    // JAL: rd = PC + 4, PC = PC + imm
    ctrl.instType   := InstType.J
    ctrl.aluOp      := ALUOp.ADD
    ctrl.branchType := BranchType.JAL
    ctrl.regWrite   := true.B
    ctrl.aluSrc1    := 1.U  // PC
    ctrl.aluSrc2    := 2.U  // 4
    ctrl.wbSrc      := 2.U  // PC+4
  }.elsewhen(opcode === "b1100111".U) {
    // JALR: rd = PC + 4, PC = (rs1 + imm) & ~1
    ctrl.instType   := InstType.I
    ctrl.aluOp      := ALUOp.ADD
    ctrl.branchType := BranchType.JALR
    ctrl.regWrite   := true.B
    ctrl.aluSrc1    := 1.U  // PC
    ctrl.aluSrc2    := 2.U  // 4
    ctrl.wbSrc      := 2.U  // PC+4
  }.elsewhen(opcode === "b1100011".U) {
    // BRANCH
    ctrl.instType := InstType.B
    ctrl.aluOp    := ALUOp.ADD
    ctrl.aluSrc1  := 1.U  // PC
    ctrl.aluSrc2  := 1.U  // imm

    when(funct3 === "b000".U) {
      ctrl.branchType := BranchType.BEQ
    }.elsewhen(funct3 === "b001".U) {
      ctrl.branchType := BranchType.BNE
    }.elsewhen(funct3 === "b100".U) {
      ctrl.branchType := BranchType.BLT
    }.elsewhen(funct3 === "b101".U) {
      ctrl.branchType := BranchType.BGE
    }.elsewhen(funct3 === "b110".U) {
      ctrl.branchType := BranchType.BLTU
    }.elsewhen(funct3 === "b111".U) {
      ctrl.branchType := BranchType.BGEU
    }
  }.elsewhen(opcode === "b0000011".U) {
    // LOAD
    ctrl.instType := InstType.I
    ctrl.aluOp    := ALUOp.ADD
    ctrl.regWrite := true.B
    ctrl.memRead  := true.B
    ctrl.aluSrc2  := 1.U  // imm
    ctrl.wbSrc    := 1.U  // MEM

    when(funct3 === "b000".U) {
      ctrl.memOp := MemOp.LB
    }.elsewhen(funct3 === "b001".U) {
      ctrl.memOp := MemOp.LH
    }.elsewhen(funct3 === "b010".U) {
      ctrl.memOp := MemOp.LW
    }.elsewhen(funct3 === "b100".U) {
      ctrl.memOp := MemOp.LBU
    }.elsewhen(funct3 === "b101".U) {
      ctrl.memOp := MemOp.LHU
    }
  }.elsewhen(opcode === "b0100011".U) {
    // STORE
    ctrl.instType := InstType.S
    ctrl.aluOp    := ALUOp.ADD
    ctrl.memWrite := true.B
    ctrl.aluSrc2  := 1.U  // imm

    when(funct3 === "b000".U) {
      ctrl.memOp := MemOp.SB
    }.elsewhen(funct3 === "b001".U) {
      ctrl.memOp := MemOp.SH
    }.elsewhen(funct3 === "b010".U) {
      ctrl.memOp := MemOp.SW
    }
  }.elsewhen(opcode === "b0010011".U) {
    // OP-IMM (I-type ALU operations)
    ctrl.instType := InstType.I
    ctrl.regWrite := true.B
    ctrl.aluSrc2  := 1.U  // imm

    when(funct3 === "b000".U) {
      ctrl.aluOp := ALUOp.ADD
    }.elsewhen(funct3 === "b010".U) {
      ctrl.aluOp := ALUOp.SLT
    }.elsewhen(funct3 === "b011".U) {
      ctrl.aluOp := ALUOp.SLTU
    }.elsewhen(funct3 === "b100".U) {
      ctrl.aluOp := ALUOp.XOR
    }.elsewhen(funct3 === "b110".U) {
      ctrl.aluOp := ALUOp.OR
    }.elsewhen(funct3 === "b111".U) {
      ctrl.aluOp := ALUOp.AND
    }.elsewhen(funct3 === "b001".U) {
      ctrl.aluOp := ALUOp.SLL
    }.elsewhen(funct3 === "b101".U) {
      when(funct7 === "b0000000".U) {
        ctrl.aluOp := ALUOp.SRL
      }.otherwise {
        ctrl.aluOp := ALUOp.SRA
      }
    }
  }.elsewhen(opcode === "b0110011".U) {
    // OP (R-type ALU operations)
    ctrl.instType := InstType.R
    ctrl.regWrite := true.B

    when(funct3 === "b000".U) {
      when(funct7 === "b0000000".U) {
        ctrl.aluOp := ALUOp.ADD
      }.otherwise {
        ctrl.aluOp := ALUOp.SUB
      }
    }.elsewhen(funct3 === "b010".U) {
      ctrl.aluOp := ALUOp.SLT
    }.elsewhen(funct3 === "b011".U) {
      ctrl.aluOp := ALUOp.SLTU
    }.elsewhen(funct3 === "b100".U) {
      ctrl.aluOp := ALUOp.XOR
    }.elsewhen(funct3 === "b110".U) {
      ctrl.aluOp := ALUOp.OR
    }.elsewhen(funct3 === "b111".U) {
      ctrl.aluOp := ALUOp.AND
    }.elsewhen(funct3 === "b001".U) {
      ctrl.aluOp := ALUOp.SLL
    }.elsewhen(funct3 === "b101".U) {
      when(funct7 === "b0000000".U) {
        ctrl.aluOp := ALUOp.SRL
      }.otherwise {
        ctrl.aluOp := ALUOp.SRA
      }
    }
  }

  io.ctrl := ctrl
}
