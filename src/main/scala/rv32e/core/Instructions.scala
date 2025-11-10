package rv32e.core

import chisel3._
import chisel3.util._

/**
 * RV32E 指令格式定义
 */
object InstType extends ChiselEnum {
  val R, I, S, B, U, J, UNKNOWN = Value
}

/**
 * ALU 操作码
 */
object ALUOp extends ChiselEnum {
  val ADD, SUB, SLL, SLT, SLTU, XOR, SRL, SRA, OR, AND,
      COPY_A, COPY_B, XXX = Value
}

/**
 * 分支类型
 */
object BranchType extends ChiselEnum {
  val NOBRANCH, BEQ, BNE, BLT, BGE, BLTU, BGEU, JAL, JALR = Value
}

/**
 * 访存操作类型
 */
object MemOp extends ChiselEnum {
  val NOP, LB, LH, LW, LBU, LHU, SB, SH, SW = Value
}

/**
 * RV32E 指令定义
 */
object Instructions {
  // Opcode 定义
  val OP_LUI    = BitPat("b0110111")
  val OP_AUIPC  = BitPat("b0010111")
  val OP_JAL    = BitPat("b1101111")
  val OP_JALR   = BitPat("b1100111")
  val OP_BRANCH = BitPat("b1100011")
  val OP_LOAD   = BitPat("b0000011")
  val OP_STORE  = BitPat("b0100011")
  val OP_IMM    = BitPat("b0010011")
  val OP_REG    = BitPat("b0110011")

  // Funct3 定义
  val F3_ADD_SUB = BitPat("b000")
  val F3_SLL     = BitPat("b001")
  val F3_SLT     = BitPat("b010")
  val F3_SLTU    = BitPat("b011")
  val F3_XOR     = BitPat("b100")
  val F3_SRL_SRA = BitPat("b101")
  val F3_OR      = BitPat("b110")
  val F3_AND     = BitPat("b111")

  val F3_BEQ     = BitPat("b000")
  val F3_BNE     = BitPat("b001")
  val F3_BLT     = BitPat("b100")
  val F3_BGE     = BitPat("b101")
  val F3_BLTU    = BitPat("b110")
  val F3_BGEU    = BitPat("b111")

  val F3_LB      = BitPat("b000")
  val F3_LH      = BitPat("b001")
  val F3_LW      = BitPat("b010")
  val F3_LBU     = BitPat("b100")
  val F3_LHU     = BitPat("b101")

  val F3_SB      = BitPat("b000")
  val F3_SH      = BitPat("b001")
  val F3_SW      = BitPat("b010")

  // Funct7 定义
  val F7_NORM    = BitPat("b0000000")
  val F7_ALT     = BitPat("b0100000")

  /**
   * 从指令中提取字段
   */
  def opcode(inst: UInt): UInt = inst(6, 0)
  def rd(inst: UInt): UInt = inst(11, 7)
  def funct3(inst: UInt): UInt = inst(14, 12)
  def rs1(inst: UInt): UInt = inst(19, 15)
  def rs2(inst: UInt): UInt = inst(24, 20)
  def funct7(inst: UInt): UInt = inst(31, 25)

  /**
   * 立即数提取
   */
  def immI(inst: UInt): UInt = inst(31, 20).asSInt.pad(32).asUInt
  def immS(inst: UInt): UInt = Cat(inst(31, 25), inst(11, 7)).asSInt.pad(32).asUInt
  def immB(inst: UInt): UInt = Cat(inst(31), inst(7), inst(30, 25), inst(11, 8), 0.U(1.W)).asSInt.pad(32).asUInt
  def immU(inst: UInt): UInt = Cat(inst(31, 12), 0.U(12.W))
  def immJ(inst: UInt): UInt = Cat(inst(31), inst(19, 12), inst(20), inst(30, 21), 0.U(1.W)).asSInt.pad(32).asUInt

  /**
   * 指令类型判断
   */
  def isRType(inst: UInt): Bool = opcode(inst) === OP_REG
  def isIType(inst: UInt): Bool = opcode(inst) === OP_IMM || opcode(inst) === OP_LOAD || opcode(inst) === OP_JALR
  def isSType(inst: UInt): Bool = opcode(inst) === OP_STORE
  def isBType(inst: UInt): Bool = opcode(inst) === OP_BRANCH
  def isUType(inst: UInt): Bool = opcode(inst) === OP_LUI || opcode(inst) === OP_AUIPC
  def isJType(inst: UInt): Bool = opcode(inst) === OP_JAL
}

/**
 * 控制信号集合
 */
class ControlSignals extends Bundle {
  val instType    = InstType()
  val aluOp       = ALUOp()
  val branchType  = BranchType()
  val memOp       = MemOp()
  val regWrite    = Bool()
  val memRead     = Bool()
  val memWrite    = Bool()
  val aluSrc1     = UInt(2.W)  // 0: rs1, 1: PC, 2: 0
  val aluSrc2     = UInt(2.W)  // 0: rs2, 1: imm, 2: 4
  val wbSrc       = UInt(2.W)  // 0: ALU, 1: MEM, 2: PC+4
}
