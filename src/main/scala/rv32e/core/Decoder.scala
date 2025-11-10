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

  // 根据 opcode 解码
  switch(opcode) {
    // LUI: rd = imm << 12
    is(OP_LUI) {
      ctrl.instType := InstType.U
      ctrl.aluOp    := ALUOp.COPY_B
      ctrl.regWrite := true.B
      ctrl.aluSrc2  := 1.U  // imm
    }

    // AUIPC: rd = PC + (imm << 12)
    is(OP_AUIPC) {
      ctrl.instType := InstType.U
      ctrl.aluOp    := ALUOp.ADD
      ctrl.regWrite := true.B
      ctrl.aluSrc1  := 1.U  // PC
      ctrl.aluSrc2  := 1.U  // imm
    }

    // JAL: rd = PC + 4, PC = PC + imm
    is(OP_JAL) {
      ctrl.instType   := InstType.J
      ctrl.aluOp      := ALUOp.ADD
      ctrl.branchType := BranchType.JAL
      ctrl.regWrite   := true.B
      ctrl.aluSrc1    := 1.U  // PC
      ctrl.aluSrc2    := 2.U  // 4
      ctrl.wbSrc      := 2.U  // PC+4
    }

    // JALR: rd = PC + 4, PC = (rs1 + imm) & ~1
    is(OP_JALR) {
      ctrl.instType   := InstType.I
      ctrl.aluOp      := ALUOp.ADD
      ctrl.branchType := BranchType.JALR
      ctrl.regWrite   := true.B
      ctrl.aluSrc1    := 1.U  // PC
      ctrl.aluSrc2    := 2.U  // 4
      ctrl.wbSrc      := 2.U  // PC+4
    }

    // BRANCH
    is(OP_BRANCH) {
      ctrl.instType := InstType.B
      ctrl.aluOp    := ALUOp.ADD
      ctrl.aluSrc1  := 1.U  // PC
      ctrl.aluSrc2  := 1.U  // imm

      switch(funct3) {
        is(F3_BEQ)  { ctrl.branchType := BranchType.BEQ }
        is(F3_BNE)  { ctrl.branchType := BranchType.BNE }
        is(F3_BLT)  { ctrl.branchType := BranchType.BLT }
        is(F3_BGE)  { ctrl.branchType := BranchType.BGE }
        is(F3_BLTU) { ctrl.branchType := BranchType.BLTU }
        is(F3_BGEU) { ctrl.branchType := BranchType.BGEU }
      }
    }

    // LOAD
    is(OP_LOAD) {
      ctrl.instType := InstType.I
      ctrl.aluOp    := ALUOp.ADD
      ctrl.regWrite := true.B
      ctrl.memRead  := true.B
      ctrl.aluSrc2  := 1.U  // imm
      ctrl.wbSrc    := 1.U  // MEM

      switch(funct3) {
        is(F3_LB)  { ctrl.memOp := MemOp.LB }
        is(F3_LH)  { ctrl.memOp := MemOp.LH }
        is(F3_LW)  { ctrl.memOp := MemOp.LW }
        is(F3_LBU) { ctrl.memOp := MemOp.LBU }
        is(F3_LHU) { ctrl.memOp := MemOp.LHU }
      }
    }

    // STORE
    is(OP_STORE) {
      ctrl.instType := InstType.S
      ctrl.aluOp    := ALUOp.ADD
      ctrl.memWrite := true.B
      ctrl.aluSrc2  := 1.U  // imm

      switch(funct3) {
        is(F3_SB) { ctrl.memOp := MemOp.SB }
        is(F3_SH) { ctrl.memOp := MemOp.SH }
        is(F3_SW) { ctrl.memOp := MemOp.SW }
      }
    }

    // OP-IMM (I-type ALU operations)
    is(OP_IMM) {
      ctrl.instType := InstType.I
      ctrl.regWrite := true.B
      ctrl.aluSrc2  := 1.U  // imm

      switch(funct3) {
        is(F3_ADD_SUB) { ctrl.aluOp := ALUOp.ADD }
        is(F3_SLT)     { ctrl.aluOp := ALUOp.SLT }
        is(F3_SLTU)    { ctrl.aluOp := ALUOp.SLTU }
        is(F3_XOR)     { ctrl.aluOp := ALUOp.XOR }
        is(F3_OR)      { ctrl.aluOp := ALUOp.OR }
        is(F3_AND)     { ctrl.aluOp := ALUOp.AND }
        is(F3_SLL)     { ctrl.aluOp := ALUOp.SLL }
        is(F3_SRL_SRA) {
          when(funct7 === F7_NORM) {
            ctrl.aluOp := ALUOp.SRL
          }.otherwise {
            ctrl.aluOp := ALUOp.SRA
          }
        }
      }
    }

    // OP (R-type ALU operations)
    is(OP_REG) {
      ctrl.instType := InstType.R
      ctrl.regWrite := true.B

      switch(funct3) {
        is(F3_ADD_SUB) {
          when(funct7 === F7_NORM) {
            ctrl.aluOp := ALUOp.ADD
          }.otherwise {
            ctrl.aluOp := ALUOp.SUB
          }
        }
        is(F3_SLT)  { ctrl.aluOp := ALUOp.SLT }
        is(F3_SLTU) { ctrl.aluOp := ALUOp.SLTU }
        is(F3_XOR)  { ctrl.aluOp := ALUOp.XOR }
        is(F3_OR)   { ctrl.aluOp := ALUOp.OR }
        is(F3_AND)  { ctrl.aluOp := ALUOp.AND }
        is(F3_SLL)  { ctrl.aluOp := ALUOp.SLL }
        is(F3_SRL_SRA) {
          when(funct7 === F7_NORM) {
            ctrl.aluOp := ALUOp.SRL
          }.otherwise {
            ctrl.aluOp := ALUOp.SRA
          }
        }
      }
    }
  }

  io.ctrl := ctrl
}
