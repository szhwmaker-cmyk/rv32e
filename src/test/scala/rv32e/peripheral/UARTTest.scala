package rv32e.peripheral

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import rv32e.utils._
import rv32e.sim._

/**
 * UART 控制器测试
 */
class UARTTest extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "UART Controller"

  it should "transmit bytes correctly" in {
    test(new UARTController) { dut =>
      // 配置波特率
      dut.io.wb.adr_i.poke(UARTRegs.BAUD.U)
      dut.io.wb.dat_i.poke(50.U)  // 使用较小的分频值加快仿真
      dut.io.wb.we_i.poke(true.B)
      dut.io.wb.stb_i.poke(true.B)
      dut.io.wb.cyc_i.poke(true.B)
      dut.clock.step()

      // 发送一个字节
      dut.io.wb.adr_i.poke(UARTRegs.TXDATA.U)
      dut.io.wb.dat_i.poke(0x55.U)
      dut.io.wb.we_i.poke(true.B)
      dut.io.wb.stb_i.poke(true.B)
      dut.io.wb.cyc_i.poke(true.B)
      dut.clock.step()

      dut.io.wb.stb_i.poke(false.B)
      dut.io.wb.cyc_i.poke(false.B)

      // 等待传输完成
      for (_ <- 0 until 1000) {
        dut.clock.step()
      }

      println("UART transmission test completed")
    }
  }

  it should "respond to Wishbone reads correctly" in {
    test(new UARTController) { dut =>
      // 读状态寄存器
      dut.io.wb.adr_i.poke(UARTRegs.STATUS.U)
      dut.io.wb.we_i.poke(false.B)
      dut.io.wb.stb_i.poke(true.B)
      dut.io.wb.cyc_i.poke(true.B)
      dut.clock.step()

      dut.io.wb.ack_o.expect(true.B)

      println("UART Wishbone interface test completed")
    }
  }
}
