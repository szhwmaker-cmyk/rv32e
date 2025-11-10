package rv32e.peripheral

import chisel3._
import chisel3.util._
import rv32e.bus._
import rv32e.utils._

/**
 * I2C Master 控制器
 * 简化版本，支持基本的读写操作
 */
class I2CMaster extends Module {
  val io = IO(new Bundle {
    // Wishbone 从设备接口
    val wb = new WishboneSlave

    // I2C 物理接口 (开漏输出)
    val scl_out = Output(Bool())
    val scl_in  = Input(Bool())
    val sda_out = Output(Bool())
    val sda_in  = Input(Bool())
  })

  // ============================================================
  // 寄存器定义
  // ============================================================
  val ctrlReg   = RegInit(0.U(32.W))
  val divReg    = RegInit(Config.I2C_CLK_DIV.U(16.W))
  val addrReg   = RegInit(0.U(8.W))
  val txData    = RegInit(0.U(8.W))
  val rxData    = RegInit(0.U(8.W))
  val statusReg = RegInit(0.U(32.W))
  val cmdReg    = RegInit(0.U(8.W))

  val busy = RegInit(false.B)
  val done = RegInit(false.B)

  // ============================================================
  // I2C 状态机 (简化版)
  // ============================================================
  val sIdle :: sStart :: sAddr :: sData :: sStop :: Nil = Enum(5)
  val state = RegInit(sIdle)

  val bitCnt = RegInit(0.U(4.W))
  val clkCnt = RegInit(0.U(16.W))
  val sclReg = RegInit(true.B)
  val sdaReg = RegInit(true.B)

  // 默认状态
  when(state === sIdle) {
    sclReg := true.B
    sdaReg := true.B
    busy   := false.B

    when(cmdReg(0)) {  // Start command
      state  := sStart
      busy   := true.B
      cmdReg := cmdReg & ~1.U
    }
  }

  // 简化的状态机实现
  // 实际应用中需要完整的 I2C 时序控制

  // ============================================================
  // Wishbone 接口逻辑
  // ============================================================
  val wb_addr = io.wb.adr_i(7, 0)
  val wb_wdata = io.wb.dat_i
  val wb_rdata = WireDefault(0.U(32.W))

  statusReg := Cat(0.U(30.W), done, busy)

  // 读操作
  switch(wb_addr) {
    is(I2CRegs.CTRL.U)   { wb_rdata := ctrlReg }
    is(I2CRegs.DIV.U)    { wb_rdata := divReg }
    is(I2CRegs.ADDR.U)   { wb_rdata := addrReg }
    is(I2CRegs.TXDATA.U) { wb_rdata := txData }
    is(I2CRegs.RXDATA.U) { wb_rdata := rxData }
    is(I2CRegs.STATUS.U) { wb_rdata := statusReg }
    is(I2CRegs.CMD.U)    { wb_rdata := cmdReg }
  }

  // 写操作
  when(io.wb.stb_i && io.wb.cyc_i && io.wb.we_i) {
    switch(wb_addr) {
      is(I2CRegs.CTRL.U)   { ctrlReg := wb_wdata }
      is(I2CRegs.DIV.U)    { divReg := wb_wdata(15, 0) }
      is(I2CRegs.ADDR.U)   { addrReg := wb_wdata(7, 0) }
      is(I2CRegs.TXDATA.U) { txData := wb_wdata(7, 0) }
      is(I2CRegs.CMD.U)    { cmdReg := wb_wdata(7, 0) }
    }
  }

  // Wishbone 响应
  io.wb.dat_o := wb_rdata
  io.wb.ack_o := io.wb.stb_i && io.wb.cyc_i

  // ============================================================
  // I2C 输出
  // ============================================================
  io.scl_out := sclReg
  io.sda_out := sdaReg
}
