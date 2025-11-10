package rv32e.peripheral

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import rv32e.utils._

/**
 * GPIO 控制器测试
 */
class GPIOTest extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "GPIO Controller"

  it should "write and read output data correctly" in {
    test(new GPIOController(32)) { dut =>
      // 设置方向为输出
      dut.io.wb.adr_i.poke(GPIORegs.DIR.U)
      dut.io.wb.dat_i.poke(0xFFFFFFFF.U)
      dut.io.wb.we_i.poke(true.B)
      dut.io.wb.stb_i.poke(true.B)
      dut.io.wb.cyc_i.poke(true.B)
      dut.clock.step()

      // 使能输出
      dut.io.wb.adr_i.poke(GPIORegs.OE.U)
      dut.io.wb.dat_i.poke(0xFFFFFFFF.U)
      dut.clock.step()

      // 写输出数据
      dut.io.wb.adr_i.poke(GPIORegs.DATA_OUT.U)
      dut.io.wb.dat_i.poke(0xAA55AA55.U)
      dut.clock.step()

      dut.io.wb.stb_i.poke(false.B)
      dut.clock.step()

      // 检查输出
      dut.io.pins_out.expect(0xAA55AA55.U)

      println("GPIO output test completed")
    }
  }

  it should "read input data correctly" in {
    test(new GPIOController(32)) { dut =>
      // 设置方向为输入
      dut.io.wb.adr_i.poke(GPIORegs.DIR.U)
      dut.io.wb.dat_i.poke(0x0.U)
      dut.io.wb.we_i.poke(true.B)
      dut.io.wb.stb_i.poke(true.B)
      dut.io.wb.cyc_i.poke(true.B)
      dut.clock.step()

      // 设置输入信号
      dut.io.pins_in.poke(0x12345678.U)
      dut.clock.step(3)  // 等待同步

      // 读输入数据
      dut.io.wb.adr_i.poke(GPIORegs.DATA_IN.U)
      dut.io.wb.we_i.poke(false.B)
      dut.clock.step()

      dut.io.wb.dat_o.expect(0x12345678.U)

      println("GPIO input test completed")
    }
  }
}
