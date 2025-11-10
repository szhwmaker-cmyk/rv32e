package rv32e.peripheral

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import rv32e.utils._

/**
 * I2C Master 控制器测试
 */
class I2CTest extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "I2C Master Controller"

  it should "initialize correctly" in {
    test(new I2CMaster) { dut =>
      // 检查初始状态
      dut.io.scl_out.expect(true.B)
      dut.io.sda_out.expect(true.B)

      println("I2C initialization test completed")
    }
  }

  it should "respond to Wishbone correctly" in {
    test(new I2CMaster) { dut =>
      // 写分频寄存器
      dut.io.wb.adr_i.poke(I2CRegs.DIV.U)
      dut.io.wb.dat_i.poke(100.U)
      dut.io.wb.we_i.poke(true.B)
      dut.io.wb.stb_i.poke(true.B)
      dut.io.wb.cyc_i.poke(true.B)
      dut.clock.step()

      dut.io.wb.ack_o.expect(true.B)

      println("I2C Wishbone interface test completed")
    }
  }
}
