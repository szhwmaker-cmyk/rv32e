package rv32e.soc

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

/**
 * RV32E SoC 集成测试
 */
class SoCTest extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "RV32E SoC"

  it should "initialize correctly" in {
    test(new RV32ESoC) { dut =>
      // 初始化输入
      dut.io.uart_rx.poke(true.B)
      dut.io.gpio_in.poke(0.U)
      dut.io.spi_miso.poke(false.B)
      dut.io.i2c_scl_in.poke(true.B)
      dut.io.i2c_sda_in.poke(true.B)
      dut.io.flash_miso.poke(false.B)

      // 运行一些周期
      dut.clock.step(100)

      println("SoC initialization test completed")
    }
  }

  it should "respond to basic operations" in {
    test(new RV32ESoC) { dut =>
      // 初始化
      dut.io.uart_rx.poke(true.B)
      dut.io.gpio_in.poke(0.U)
      dut.io.spi_miso.poke(false.B)
      dut.io.i2c_scl_in.poke(true.B)
      dut.io.i2c_sda_in.poke(true.B)
      dut.io.flash_miso.poke(false.B)

      // 运行更多周期
      for (_ <- 0 until 1000) {
        dut.clock.step()
      }

      println("SoC basic operations test completed")
    }
  }
}
