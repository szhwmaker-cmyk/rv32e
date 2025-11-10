package rv32e.soc

import chisel3._

/**
 * Verilog 生成器
 * 使用 Chisel 的 getVerilogString API
 */
object VerilogGenerator extends App {
  println(getVerilogString(new RV32ESoC))
}
