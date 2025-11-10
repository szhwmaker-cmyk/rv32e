package rv32e.core

import chisel3._
import chisel3.util._
import rv32e.utils.Config

/**
 * RV32E 寄存器文件
 * 16 个通用寄存器 (x0-x15)
 * x0 恒为 0
 * 支持 2 读 1 写
 */
class RegFile extends Module {
  val io = IO(new Bundle {
    // 读端口 1
    val rs1_addr = Input(UInt(4.W))
    val rs1_data = Output(UInt(32.W))

    // 读端口 2
    val rs2_addr = Input(UInt(4.W))
    val rs2_data = Output(UInt(32.W))

    // 写端口
    val wen      = Input(Bool())
    val waddr    = Input(UInt(4.W))
    val wdata    = Input(UInt(32.W))
  })

  // 16 个 32 位寄存器
  val regs = Reg(Vec(Config.NUM_REGS, UInt(32.W)))

  // 读操作 (组合逻辑)
  // x0 恒为 0，其他寄存器正常读取
  io.rs1_data := Mux(io.rs1_addr === 0.U, 0.U, regs(io.rs1_addr))
  io.rs2_data := Mux(io.rs2_addr === 0.U, 0.U, regs(io.rs2_addr))

  // 写操作 (时序逻辑)
  // 写 x0 无效
  when(io.wen && io.waddr =/= 0.U) {
    regs(io.waddr) := io.wdata
  }
}
