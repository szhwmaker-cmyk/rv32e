package rv32e.sim

import chisel3._
import chisel3.util._

/**
 * UART 仿真模型
 * 模拟外部 UART 设备的行为，用于测试
 */
class UARTModel(baudDiv: Int = 434) extends Module {
  val io = IO(new Bundle {
    val tx = Input(Bool())   // 接收来自 UART 控制器的数据
    val rx = Output(Bool())  // 发送数据到 UART 控制器

    // 测试接口
    val rxByte = Output(UInt(8.W))
    val rxValid = Output(Bool())
    val txByte = Input(UInt(8.W))
    val txStart = Input(Bool())
  })

  // ============================================================
  // 接收器 (从 UART TX 接收)
  // ============================================================
  val rxState = RegInit(0.U(2.W))
  val rxBitCnt = RegInit(0.U(3.W))
  val rxShiftReg = RegInit(0.U(8.W))
  val rxBaudCnt = RegInit(0.U(16.W))
  val rxSync = RegNext(RegNext(io.tx))
  val rxByteReg = RegInit(0.U(8.W))
  val rxValidReg = RegInit(false.B)

  rxValidReg := false.B  // 默认每个周期清除

  when(rxBaudCnt > 0.U) {
    rxBaudCnt := rxBaudCnt - 1.U
  }.otherwise {
    rxBaudCnt := baudDiv.U

    switch(rxState) {
      is(0.U) {  // IDLE
        when(!rxSync) {
          rxState := 1.U
          rxBaudCnt := (baudDiv / 2).U
        }
      }
      is(1.U) {  // START
        when(!rxSync) {
          rxState := 2.U
          rxBitCnt := 0.U
          rxBaudCnt := baudDiv.U
        }.otherwise {
          rxState := 0.U
        }
      }
      is(2.U) {  // DATA
        rxShiftReg := Cat(rxSync, rxShiftReg(7, 1))
        when(rxBitCnt === 7.U) {
          rxState := 3.U
        }.otherwise {
          rxBitCnt := rxBitCnt + 1.U
        }
      }
      is(3.U) {  // STOP
        when(rxSync) {
          rxByteReg := rxShiftReg
          rxValidReg := true.B
        }
        rxState := 0.U
      }
    }
  }

  io.rxByte := rxByteReg
  io.rxValid := rxValidReg

  // ============================================================
  // 发送器 (发送到 UART RX)
  // ============================================================
  val txState = RegInit(0.U(2.W))
  val txBitCnt = RegInit(0.U(3.W))
  val txShiftReg = RegInit(0.U(8.W))
  val txBaudCnt = RegInit(0.U(16.W))
  val txOut = RegInit(true.B)

  when(txBaudCnt > 0.U) {
    txBaudCnt := txBaudCnt - 1.U
  }.otherwise {
    txBaudCnt := baudDiv.U

    switch(txState) {
      is(0.U) {  // IDLE
        txOut := true.B
        when(io.txStart) {
          txState := 1.U
          txShiftReg := io.txByte
        }
      }
      is(1.U) {  // START
        txOut := false.B
        txState := 2.U
        txBitCnt := 0.U
      }
      is(2.U) {  // DATA
        txOut := txShiftReg(0)
        txShiftReg := txShiftReg >> 1
        when(txBitCnt === 7.U) {
          txState := 3.U
        }.otherwise {
          txBitCnt := txBitCnt + 1.U
        }
      }
      is(3.U) {  // STOP
        txOut := true.B
        txState := 0.U
      }
    }
  }

  io.rx := txOut
}
