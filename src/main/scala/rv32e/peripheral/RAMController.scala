package rv32e.peripheral

import chisel3._
import chisel3.util._
import rv32e.bus._

/**
 * RAM 控制器
 * 提供 Wishbone 接口访问片上 RAM
 */
class RAMController(sizeBytes: Int = 64 * 1024) extends Module {
  val io = IO(new Bundle {
    // Wishbone 从设备接口
    val wb = new WishboneSlave
  })

  // RAM 存储器
  val mem = SyncReadMem(sizeBytes / 4, UInt(32.W))

  // 地址计算 (字地址)
  val wordAddr = io.wb.adr_i(log2Ceil(sizeBytes) - 1, 2)

  // 读操作
  val readData = mem.read(wordAddr)

  // 写操作
  val writeEnable = io.wb.stb_i && io.wb.cyc_i && io.wb.we_i
  val writeMask = io.wb.sel_i

  when(writeEnable) {
    // 字节写使能
    val wdata = Wire(UInt(32.W))
    val oldData = readData

    val byte0 = Mux(writeMask(0), io.wb.dat_i(7, 0), oldData(7, 0))
    val byte1 = Mux(writeMask(1), io.wb.dat_i(15, 8), oldData(15, 8))
    val byte2 = Mux(writeMask(2), io.wb.dat_i(23, 16), oldData(23, 16))
    val byte3 = Mux(writeMask(3), io.wb.dat_i(31, 24), oldData(31, 24))

    wdata := Cat(byte3, byte2, byte1, byte0)
    mem.write(wordAddr, wdata)
  }

  // Wishbone 响应
  io.wb.dat_o := readData
  io.wb.ack_o := io.wb.stb_i && io.wb.cyc_i
}
