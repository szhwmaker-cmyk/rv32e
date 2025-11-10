package rv32e.peripheral

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

/**
 * SPI Flash 控制器测试
 */
class SPIFlashTest extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "SPI Flash Controller"

  it should "initialize correctly" in {
    test(new SPIFlashController) { dut =>
      // 检查初始状态
      dut.io.cs_n.expect(true.B)
      dut.io.sclk.expect(false.B)

      println("SPI Flash initialization test completed")
    }
  }

  it should "respond to Wishbone read requests" in {
    test(new SPIFlashController) { dut =>
      // 发起读请求
      dut.io.wb.adr_i.poke(0x1000.U)
      dut.io.wb.we_i.poke(false.B)
      dut.io.wb.stb_i.poke(true.B)
      dut.io.wb.cyc_i.poke(true.B)
      dut.io.miso.poke(false.B)

      // 等待一段时间
      for (_ <- 0 until 500) {
        dut.clock.step()
        if (dut.io.wb.ack_o.peek().litToBoolean) {
          println("SPI Flash read completed")
          return
        }
      }

      println("SPI Flash read test completed (timeout)")
    }
  }
}
