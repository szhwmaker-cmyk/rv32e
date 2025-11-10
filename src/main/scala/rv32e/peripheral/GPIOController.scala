package rv32e.peripheral

import chisel3._
import chisel3.util._
import rv32e.bus._
import rv32e.utils._

/**
 * GPIO 控制器
 * 支持 32 个 GPIO 引脚的输入/输出控制
 */
class GPIOController(numPins: Int = 32) extends Module {
  val io = IO(new Bundle {
    // Wishbone 从设备接口
    val wb = new WishboneSlave

    // GPIO 物理引脚
    val pins_in  = Input(UInt(numPins.W))
    val pins_out = Output(UInt(numPins.W))
    val pins_oe  = Output(UInt(numPins.W))  // 输出使能
  })

  // ============================================================
  // 寄存器定义
  // ============================================================
  val dataIn  = RegInit(0.U(numPins.W))
  val dataOut = RegInit(0.U(numPins.W))
  val dir     = RegInit(0.U(numPins.W))  // 0: 输入, 1: 输出
  val oe      = RegInit(0.U(numPins.W))  // 输出使能

  // 输入数据采样（双寄存器同步）
  val pinsSync1 = RegNext(io.pins_in)
  val pinsSync2 = RegNext(pinsSync1)
  dataIn := pinsSync2

  // ============================================================
  // Wishbone 接口逻辑
  // ============================================================
  val wb_addr = io.wb.adr_i(7, 0)
  val wb_wdata = io.wb.dat_i
  val wb_rdata = WireDefault(0.U(32.W))

  // 读操作
  switch(wb_addr) {
    is(GPIORegs.DATA_IN.U) {
      wb_rdata := dataIn
    }
    is(GPIORegs.DATA_OUT.U) {
      wb_rdata := dataOut
    }
    is(GPIORegs.DIR.U) {
      wb_rdata := dir
    }
    is(GPIORegs.OE.U) {
      wb_rdata := oe
    }
  }

  // 写操作
  when(io.wb.stb_i && io.wb.cyc_i && io.wb.we_i) {
    switch(wb_addr) {
      is(GPIORegs.DATA_OUT.U) {
        dataOut := wb_wdata(numPins - 1, 0)
      }
      is(GPIORegs.DIR.U) {
        dir := wb_wdata(numPins - 1, 0)
      }
      is(GPIORegs.OE.U) {
        oe := wb_wdata(numPins - 1, 0)
      }
    }
  }

  // Wishbone 响应
  io.wb.dat_o := wb_rdata
  io.wb.ack_o := io.wb.stb_i && io.wb.cyc_i

  // ============================================================
  // GPIO 输出
  // ============================================================
  io.pins_out := dataOut
  io.pins_oe  := dir & oe  // 只有方向为输出且使能时才输出
}
