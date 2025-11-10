package rv32e.peripheral

import chisel3._
import chisel3.util._
import rv32e.bus._
import rv32e.utils._

/**
 * SPI Master 控制器
 * 支持标准 SPI 模式 0 (CPOL=0, CPHA=0)
 */
class SPIMaster extends Module {
  val io = IO(new Bundle {
    // Wishbone 从设备接口
    val wb = new WishboneSlave

    // SPI 物理接口
    val sclk = Output(Bool())
    val mosi = Output(Bool())
    val miso = Input(Bool())
    val cs_n = Output(Bool())  // 片选 (低电平有效)
  })

  // ============================================================
  // 寄存器定义
  // ============================================================
  val ctrlReg   = RegInit(0.U(32.W))
  val divReg    = RegInit(Config.SPI_CLK_DIV.U(16.W))
  val txData    = RegInit(0.U(8.W))
  val rxData    = RegInit(0.U(8.W))
  val status    = RegInit(0.U(32.W))

  val busy      = RegInit(false.B)
  val done      = RegInit(false.B)

  // ============================================================
  // SPI 传输状态机
  // ============================================================
  val sIdle :: sTransfer :: sDone :: Nil = Enum(3)
  val state = RegInit(sIdle)

  val bitCnt    = RegInit(0.U(3.W))
  val clkCnt    = RegInit(0.U(16.W))
  val shiftReg  = RegInit(0.U(8.W))
  val sclkReg   = RegInit(false.B)
  val csReg     = RegInit(true.B)  // 默认不选中
  val misoSync  = RegNext(RegNext(io.miso))

  switch(state) {
    is(sIdle) {
      sclkReg := false.B
      csReg   := true.B
      busy    := false.B
      done    := false.B

      when(ctrlReg(0)) {  // Start bit
        state    := sTransfer
        shiftReg := txData
        bitCnt   := 0.U
        clkCnt   := 0.U
        csReg    := false.B
        busy     := true.B
        ctrlReg  := ctrlReg & ~1.U  // 清除 start bit
      }
    }

    is(sTransfer) {
      when(clkCnt === divReg) {
        clkCnt := 0.U
        sclkReg := !sclkReg

        when(sclkReg) {
          // 上升沿：采样 MISO
          shiftReg := Cat(shiftReg(6, 0), misoSync)
          bitCnt := bitCnt + 1.U

          when(bitCnt === 7.U) {
            state := sDone
          }
        }.otherwise {
          // 下降沿：输出 MOSI (准备下一位)
        }
      }.otherwise {
        clkCnt := clkCnt + 1.U
      }
    }

    is(sDone) {
      sclkReg := false.B
      csReg   := true.B
      rxData  := shiftReg
      done    := true.B
      busy    := false.B
      state   := sIdle
    }
  }

  // ============================================================
  // Wishbone 接口逻辑
  // ============================================================
  val wb_addr = io.wb.adr_i(7, 0)
  val wb_wdata = io.wb.dat_i
  val wb_rdata = WireDefault(0.U(32.W))

  // 状态寄存器
  status := Cat(0.U(30.W), done, busy)

  // 读操作
  switch(wb_addr) {
    is(SPIRegs.CTRL.U) {
      wb_rdata := ctrlReg
    }
    is(SPIRegs.DIV.U) {
      wb_rdata := divReg
    }
    is(SPIRegs.TXDATA.U) {
      wb_rdata := txData
    }
    is(SPIRegs.RXDATA.U) {
      wb_rdata := rxData
    }
    is(SPIRegs.STATUS.U) {
      wb_rdata := status
    }
  }

  // 写操作
  when(io.wb.stb_i && io.wb.cyc_i && io.wb.we_i) {
    switch(wb_addr) {
      is(SPIRegs.CTRL.U) {
        ctrlReg := wb_wdata
      }
      is(SPIRegs.DIV.U) {
        divReg := wb_wdata(15, 0)
      }
      is(SPIRegs.TXDATA.U) {
        txData := wb_wdata(7, 0)
      }
    }
  }

  // Wishbone 响应
  io.wb.dat_o := wb_rdata
  io.wb.ack_o := io.wb.stb_i && io.wb.cyc_i

  // ============================================================
  // SPI 输出
  // ============================================================
  io.sclk := sclkReg
  io.mosi := shiftReg(7)
  io.cs_n := csReg
}
