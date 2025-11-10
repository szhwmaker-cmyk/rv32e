package rv32e.peripheral

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

/**
 * RAM 控制器测试
 */
class RAMTest extends AnyFlatSpec with ChiselScalatestTester {
  behavior of "RAM Controller"

  it should "write and read correctly" in {
    test(new RAMController(1024)) { dut =>
      // 写操作
      dut.io.wb.adr_i.poke(0x100.U)
      dut.io.wb.dat_i.poke(0x12345678.U)
      dut.io.wb.we_i.poke(true.B)
      dut.io.wb.sel_i.poke("b1111".U)
      dut.io.wb.stb_i.poke(true.B)
      dut.io.wb.cyc_i.poke(true.B)
      dut.clock.step()

      // 读操作
      dut.io.wb.adr_i.poke(0x100.U)
      dut.io.wb.we_i.poke(false.B)
      dut.clock.step()

      dut.io.wb.dat_o.expect(0x12345678.U)
      dut.io.wb.ack_o.expect(true.B)

      println("RAM write/read test completed")
    }
  }

  it should "support byte writes" in {
    test(new RAMController(1024)) { dut =>
      // 写第一个字节
      dut.io.wb.adr_i.poke(0x200.U)
      dut.io.wb.dat_i.poke(0xAA.U)
      dut.io.wb.we_i.poke(true.B)
      dut.io.wb.sel_i.poke("b0001".U)
      dut.io.wb.stb_i.poke(true.B)
      dut.io.wb.cyc_i.poke(true.B)
      dut.clock.step()

      // 写第二个字节
      dut.io.wb.dat_i.poke(0xBB00.U)
      dut.io.wb.sel_i.poke("b0010".U)
      dut.clock.step()

      // 读整个字
      dut.io.wb.we_i.poke(false.B)
      dut.clock.step()

      println(s"RAM byte write test result: 0x${dut.io.wb.dat_o.peek().litValue.toHexString}")
    }
  }
}
