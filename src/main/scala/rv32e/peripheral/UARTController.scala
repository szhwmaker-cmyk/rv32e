package rv32e.peripheral

import chisel3._
import chisel3.util._
import rv32e.bus._
import rv32e.utils._

/**
 * UART 控制器
 * 支持可配置波特率的串口通信
 */
class UARTController extends Module {
  val io = IO(new Bundle {
    // Wishbone 从设备接口
    val wb = new WishboneSlave

    // UART 物理接口
    val tx = Output(Bool())
    val rx = Input(Bool())
  })

  // ============================================================
  // 寄存器定义
  // ============================================================
  val txData   = RegInit(0.U(8.W))
  val rxData   = RegInit(0.U(8.W))
  val baudDiv  = RegInit(Config.UART_DIV.U(16.W))
  val txEnable = RegInit(true.B)
  val rxEnable = RegInit(true.B)

  // 状态寄存器
  val txBusy   = RegInit(false.B)
  val txEmpty  = RegInit(true.B)
  val rxValid  = RegInit(false.B)

  // FIFO (简化版，单个寄存器)
  val txFifo   = RegInit(0.U(8.W))
  val txFull   = RegInit(false.B)

  // ============================================================
  // Wishbone 接口逻辑
  // ============================================================
  val wb_addr = io.wb.adr_i(7, 0)
  val wb_wdata = io.wb.dat_i
  val wb_rdata = WireDefault(0.U(32.W))

  // 读操作
  switch(wb_addr) {
    is(UARTRegs.TXDATA.U) {
      wb_rdata := 0.U
    }
    is(UARTRegs.RXDATA.U) {
      wb_rdata := rxData
    }
    is(UARTRegs.STATUS.U) {
      wb_rdata := Cat(
        0.U(24.W),
        txFull,
        txEmpty,
        rxValid,
        txBusy,
        rxEnable,
        txEnable,
        0.U(2.W)
      )
    }
    is(UARTRegs.BAUD.U) {
      wb_rdata := baudDiv
    }
    is(UARTRegs.CTRL.U) {
      wb_rdata := Cat(0.U(30.W), rxEnable, txEnable)
    }
  }

  // 写操作
  val txStart = WireDefault(false.B)
  when(io.wb.stb_i && io.wb.cyc_i && io.wb.we_i) {
    switch(wb_addr) {
      is(UARTRegs.TXDATA.U) {
        when(!txFull) {
          txFifo  := wb_wdata(7, 0)
          txFull  := true.B
          txEmpty := false.B
        }
      }
      is(UARTRegs.BAUD.U) {
        baudDiv := wb_wdata(15, 0)
      }
      is(UARTRegs.CTRL.U) {
        txEnable := wb_wdata(0)
        rxEnable := wb_wdata(1)
      }
    }
  }

  // Wishbone 响应
  io.wb.dat_o := wb_rdata
  io.wb.ack_o := io.wb.stb_i && io.wb.cyc_i

  // ============================================================
  // UART 发送器
  // ============================================================
  val txState = RegInit(0.U(2.W))  // 0: IDLE, 1: START, 2: DATA, 3: STOP
  val txBitCnt = RegInit(0.U(3.W))
  val txShiftReg = RegInit(0.U(8.W))
  val txBaudCnt = RegInit(0.U(16.W))
  val txOut = RegInit(true.B)  // 空闲时为高

  when(txEnable) {
    when(txBaudCnt > 0.U) {
      txBaudCnt := txBaudCnt - 1.U
    }.otherwise {
      txBaudCnt := baudDiv

      switch(txState) {
        is(0.U) {  // IDLE
          txOut := true.B
          txBusy := false.B
          when(txFull) {
            txState := 1.U
            txShiftReg := txFifo
            txFull := false.B
            txBusy := true.B
          }
        }
        is(1.U) {  // START BIT
          txOut := false.B
          txState := 2.U
          txBitCnt := 0.U
        }
        is(2.U) {  // DATA BITS
          txOut := txShiftReg(0)
          txShiftReg := txShiftReg >> 1
          when(txBitCnt === 7.U) {
            txState := 3.U
          }.otherwise {
            txBitCnt := txBitCnt + 1.U
          }
        }
        is(3.U) {  // STOP BIT
          txOut := true.B
          txState := 0.U
          txEmpty := !txFull
        }
      }
    }
  }

  io.tx := txOut

  // ============================================================
  // UART 接收器
  // ============================================================
  val rxState = RegInit(0.U(2.W))  // 0: IDLE, 1: START, 2: DATA, 3: STOP
  val rxBitCnt = RegInit(0.U(3.W))
  val rxShiftReg = RegInit(0.U(8.W))
  val rxBaudCnt = RegInit(0.U(16.W))
  val rxSync = RegNext(RegNext(io.rx))  // 双寄存器同步

  when(rxEnable) {
    when(rxBaudCnt > 0.U) {
      rxBaudCnt := rxBaudCnt - 1.U
    }.otherwise {
      rxBaudCnt := baudDiv

      switch(rxState) {
        is(0.U) {  // IDLE
          when(!rxSync) {  // 检测到起始位
            rxState := 1.U
            rxBaudCnt := baudDiv >> 1  // 在位中间采样
          }
        }
        is(1.U) {  // START BIT
          when(!rxSync) {  // 确认起始位
            rxState := 2.U
            rxBitCnt := 0.U
            rxBaudCnt := baudDiv
          }.otherwise {
            rxState := 0.U  // 错误，回到空闲
          }
        }
        is(2.U) {  // DATA BITS
          rxShiftReg := Cat(rxSync, rxShiftReg(7, 1))
          when(rxBitCnt === 7.U) {
            rxState := 3.U
          }.otherwise {
            rxBitCnt := rxBitCnt + 1.U
          }
        }
        is(3.U) {  // STOP BIT
          when(rxSync) {  // 确认停止位
            rxData := rxShiftReg
            rxValid := true.B
          }
          rxState := 0.U
        }
      }
    }
  }

  // 读取后清除 rxValid
  when(io.wb.stb_i && io.wb.cyc_i && !io.wb.we_i && wb_addr === UARTRegs.RXDATA.U) {
    rxValid := false.B
  }
}
