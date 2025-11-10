package rv32e.bus

import chisel3._
import chisel3.util._

/**
 * Wishbone B4 Pipeline 主设备接口
 * 符合 Wishbone B4 规范
 */
class WishboneMaster extends Bundle {
  // Master → Slave 信号
  val adr_o = Output(UInt(32.W))   // 地址线
  val dat_o = Output(UInt(32.W))   // 写数据
  val we_o  = Output(Bool())       // 写使能 (1=写, 0=读)
  val sel_o = Output(UInt(4.W))    // 字节选择 (4 bits for 32-bit bus)
  val stb_o = Output(Bool())       // Strobe 信号 (传输有效)
  val cyc_o = Output(Bool())       // Cycle 信号 (总线周期有效)

  // Slave → Master 信号
  val dat_i = Input(UInt(32.W))    // 读数据
  val ack_i = Input(Bool())        // 应答信号 (传输完成)
}

/**
 * Wishbone B4 Pipeline 从设备接口
 */
class WishboneSlave extends Bundle {
  // Master → Slave 信号
  val adr_i = Input(UInt(32.W))    // 地址线
  val dat_i = Input(UInt(32.W))    // 写数据
  val we_i  = Input(Bool())        // 写使能
  val sel_i = Input(UInt(4.W))     // 字节选择
  val stb_i = Input(Bool())        // Strobe 信号
  val cyc_i = Input(Bool())        // Cycle 信号

  // Slave → Master 信号
  val dat_o = Output(UInt(32.W))   // 读数据
  val ack_o = Output(Bool())       // 应答信号
}

/**
 * Wishbone 工具对象，提供辅助函数
 */
object Wishbone {
  /**
   * 连接主设备和从设备
   */
  def connect(master: WishboneMaster, slave: WishboneSlave): Unit = {
    // Master → Slave
    slave.adr_i := master.adr_o
    slave.dat_i := master.dat_o
    slave.we_i  := master.we_o
    slave.sel_i := master.sel_o
    slave.stb_i := master.stb_o
    slave.cyc_i := master.cyc_o

    // Slave → Master
    master.dat_i := slave.dat_o
    master.ack_i := slave.ack_o
  }

  /**
   * 创建空闲的主设备信号
   */
  def idleMaster(master: WishboneMaster): Unit = {
    master.adr_o := 0.U
    master.dat_o := 0.U
    master.we_o  := false.B
    master.sel_o := 0.U
    master.stb_o := false.B
    master.cyc_o := false.B
  }

  /**
   * 创建空闲的从设备信号
   */
  def idleSlave(slave: WishboneSlave): Unit = {
    slave.dat_o := 0.U
    slave.ack_o := false.B
  }
}
