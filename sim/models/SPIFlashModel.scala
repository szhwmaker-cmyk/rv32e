package rv32e.sim

import chisel3._
import chisel3.util._

/**
 * SPI Flash 仿真模型
 * 模拟 SPI Flash 存储器的行为
 */
class SPIFlashModel(sizeBytes: Int = 1024 * 1024) extends Module {
  val io = IO(new Bundle {
    val sclk = Input(Bool())
    val mosi = Input(Bool())
    val miso = Output(Bool())
    val cs_n = Input(Bool())
  })

  // Flash 存储器 (使用 Mem 实现)
  val mem = Mem(sizeBytes, UInt(8.W))

  // 状态机
  val sIdle :: sCmd :: sAddr :: sDummy :: sData :: Nil = Enum(5)
  val state = RegInit(sIdle)

  val bitCnt = RegInit(0.U(8.W))
  val shiftReg = RegInit(0.U(32.W))
  val cmd = RegInit(0.U(8.W))
  val addr = RegInit(0.U(24.W))
  val misoReg = RegInit(false.B)

  val sclkPrev = RegNext(io.sclk)
  val risingEdge = io.sclk && !sclkPrev
  val fallingEdge = !io.sclk && sclkPrev

  when(io.cs_n) {
    state := sIdle
    bitCnt := 0.U
    misoReg := false.B
  }.otherwise {
    switch(state) {
      is(sIdle) {
        when(risingEdge) {
          state := sCmd
          bitCnt := 0.U
        }
      }

      is(sCmd) {
        when(risingEdge) {
          shiftReg := Cat(shiftReg(30, 0), io.mosi)
          bitCnt := bitCnt + 1.U

          when(bitCnt === 7.U) {
            cmd := Cat(shiftReg(6, 0), io.mosi)
            state := sAddr
            bitCnt := 0.U
          }
        }
      }

      is(sAddr) {
        when(risingEdge) {
          shiftReg := Cat(shiftReg(30, 0), io.mosi)
          bitCnt := bitCnt + 1.U

          when(bitCnt === 23.U) {
            addr := Cat(shiftReg(22, 0), io.mosi)
            state := sDummy
            bitCnt := 0.U
          }
        }
      }

      is(sDummy) {
        when(risingEdge) {
          bitCnt := bitCnt + 1.U
          when(bitCnt === 7.U) {
            state := sData
            bitCnt := 0.U
          }
        }
      }

      is(sData) {
        when(fallingEdge) {
          // 输出数据
          val byteAddr = (addr + (bitCnt >> 3)) % sizeBytes.U
          val byte = mem.read(byteAddr)
          val bitPos = 7.U - (bitCnt(2, 0))
          misoReg := (byte >> bitPos)(0)
          bitCnt := bitCnt + 1.U
        }
      }
    }
  }

  io.miso := misoReg
}
