package rv32e.peripheral

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import rv32e.utils._

/**
 * SPI Master 控制器测试
 */
class SPITest extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "SPI Master Controller"

  it should "initialize correctly" in {
    test(new SPIMaster) { dut =>
      // 检查初始状态
      dut.io.cs_n.expect(true.B)
      dut.io.sclk.expect(false.B)

      println("SPI initialization test completed")
    }
  }

  it should "respond to Wishbone correctly" in {
    test(new SPIMaster) { dut =>
      // 写分频寄存器
      dut.io.wb.adr_i.poke(SPIRegs.DIV.U)
      dut.io.wb.dat_i.poke(4.U)
      dut.io.wb.we_i.poke(true.B)
      dut.io.wb.stb_i.poke(true.B)
      dut.io.wb.cyc_i.poke(true.B)
      dut.clock.step()

      dut.io.wb.ack_o.expect(true.B)

      println("SPI Wishbone interface test completed")
    }
  }
}
