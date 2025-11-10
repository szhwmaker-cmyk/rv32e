package rv32e.core

import chisel3._
import chisel3.util._

/**
 * ALU (算术逻辑单元)
 * 支持 RV32E 所需的所有算术和逻辑运算
 */
class ALU extends Module {
  val io = IO(new Bundle {
    val op   = Input(ALUOp())      // ALU 操作码
    val src1 = Input(UInt(32.W))   // 操作数 1
    val src2 = Input(UInt(32.W))   // 操作数 2
    val out  = Output(UInt(32.W))  // 运算结果
    val zero = Output(Bool())      // 结果是否为 0
  })

  // 默认输出
  val result = WireDefault(0.U(32.W))

  // 根据操作码执行运算
  switch(io.op) {
    is(ALUOp.ADD) {
      result := io.src1 + io.src2
    }
    is(ALUOp.SUB) {
      result := io.src1 - io.src2
    }
    is(ALUOp.SLL) {
      result := io.src1 << io.src2(4, 0)
    }
    is(ALUOp.SLT) {
      result := (io.src1.asSInt < io.src2.asSInt).asUInt
    }
    is(ALUOp.SLTU) {
      result := (io.src1 < io.src2).asUInt
    }
    is(ALUOp.XOR) {
      result := io.src1 ^ io.src2
    }
    is(ALUOp.SRL) {
      result := io.src1 >> io.src2(4, 0)
    }
    is(ALUOp.SRA) {
      result := (io.src1.asSInt >> io.src2(4, 0)).asUInt
    }
    is(ALUOp.OR) {
      result := io.src1 | io.src2
    }
    is(ALUOp.AND) {
      result := io.src1 & io.src2
    }
    is(ALUOp.COPY_A) {
      result := io.src1
    }
    is(ALUOp.COPY_B) {
      result := io.src2
    }
  }

  io.out  := result
  io.zero := result === 0.U
}

/**
 * 分支比较单元
 * 用于判断分支条件是否满足
 */
class BranchComp extends Module {
  val io = IO(new Bundle {
    val branchType = Input(BranchType())
    val src1       = Input(UInt(32.W))
    val src2       = Input(UInt(32.W))
    val taken      = Output(Bool())
  })

  val eq  = io.src1 === io.src2
  val lt  = io.src1.asSInt < io.src2.asSInt
  val ltu = io.src1 < io.src2

  io.taken := MuxLookup(io.branchType, false.B)(Seq(
    BranchType.BEQ  -> eq,
    BranchType.BNE  -> !eq,
    BranchType.BLT  -> lt,
    BranchType.BGE  -> !lt,
    BranchType.BLTU -> ltu,
    BranchType.BGEU -> !ltu,
    BranchType.JAL  -> true.B,
    BranchType.JALR -> true.B
  ))
}
