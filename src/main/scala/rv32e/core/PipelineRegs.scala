package rv32e.core

import chisel3._
import chisel3.util._

/**
 * IF/ID 流水线寄存器
 */
class IF_ID extends Bundle {
  val pc   = UInt(32.W)
  val inst = UInt(32.W)
}

/**
 * ID/EX 流水线寄存器
 */
class ID_EX extends Bundle {
  val pc          = UInt(32.W)
  val rs1_data    = UInt(32.W)
  val rs2_data    = UInt(32.W)
  val imm         = UInt(32.W)
  val rs1_addr    = UInt(4.W)
  val rs2_addr    = UInt(4.W)
  val rd_addr     = UInt(4.W)
  val ctrl        = new ControlSignals
}

/**
 * EX/MEM 流水线寄存器
 */
class EX_MEM extends Bundle {
  val pc          = UInt(32.W)
  val alu_result  = UInt(32.W)
  val rs2_data    = UInt(32.W)
  val rd_addr     = UInt(4.W)
  val branch_target = UInt(32.W)
  val branch_taken  = Bool()
  val ctrl        = new ControlSignals
}

/**
 * MEM/WB 流水线寄存器
 */
class MEM_WB extends Bundle {
  val pc          = UInt(32.W)
  val alu_result  = UInt(32.W)
  val mem_data    = UInt(32.W)
  val rd_addr     = UInt(4.W)
  val ctrl        = new ControlSignals
}
