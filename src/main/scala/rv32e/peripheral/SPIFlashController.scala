package rv32e.peripheral

import chisel3._
import chisel3.util._
import rv32e.bus._

/**
 * SPI Flash 控制器
 * 支持 Fast Read (0x0B) 命令
 * 实现内存映射访问接口
 */
class SPIFlashController extends Module {
  val io = IO(new Bundle {
    // Wishbone 从设备接口
    val wb = new WishboneSlave

    // SPI Flash 物理接口
    val sclk = Output(Bool())
    val mosi = Output(Bool())
    val miso = Input(Bool())
    val cs_n = Output(Bool())
  })

  // ============================================================
  // SPI Flash 读取状态机
  // ============================================================
  val sIdle :: sCmd :: sAddr :: sDummy :: sData :: Nil = Enum(5)
  val state = RegInit(sIdle)

  val clkDiv = RegInit(4.U(4.W))  // SPI 时钟分频
  val clkCnt = RegInit(0.U(4.W))
  val bitCnt = RegInit(0.U(5.W))
  val shiftReg = RegInit(0.U(32.W))
  val addrReg = RegInit(0.U(24.W))
  val dataReg = RegInit(0.U(32.W))

  val sclkReg = RegInit(false.B)
  val mosiReg = RegInit(false.B)
  val csReg = RegInit(true.B)
  val misoSync = RegNext(RegNext(io.miso))

  val readPending = RegInit(false.B)
  val readDone = RegInit(false.B)

  // Wishbone 请求捕获
  val wbReq = io.wb.stb_i && io.wb.cyc_i && !io.wb.we_i
  when(wbReq && state === sIdle) {
    readPending := true.B
    addrReg := io.wb.adr_i(23, 0)
  }

  // SPI 时钟分频
  val spiClkTick = WireDefault(false.B)
  when(clkCnt === clkDiv) {
    clkCnt := 0.U
    spiClkTick := true.B
  }.otherwise {
    clkCnt := clkCnt + 1.U
  }

  // 状态机
  switch(state) {
    is(sIdle) {
      csReg := true.B
      sclkReg := false.B
      readDone := false.B

      when(readPending) {
        state := sCmd
        csReg := false.B
        shiftReg := 0x0B000000.U  // Fast Read command
        bitCnt := 0.U
        readPending := false.B
      }
    }

    is(sCmd) {  // 发送命令字节 (0x0B)
      when(spiClkTick) {
        sclkReg := !sclkReg

        when(!sclkReg) {
          // 下降沿：输出 MOSI
          mosiReg := shiftReg(7)
          shiftReg := Cat(shiftReg(6, 0), 0.U(1.W))
        }.otherwise {
          // 上升沿：计数
          bitCnt := bitCnt + 1.U
          when(bitCnt === 7.U) {
            state := sAddr
            shiftReg := Cat(addrReg, 0.U(8.W))
            bitCnt := 0.U
          }
        }
      }
    }

    is(sAddr) {  // 发送地址 (24 bits)
      when(spiClkTick) {
        sclkReg := !sclkReg

        when(!sclkReg) {
          mosiReg := shiftReg(31)
          shiftReg := Cat(shiftReg(30, 0), 0.U(1.W))
        }.otherwise {
          bitCnt := bitCnt + 1.U
          when(bitCnt === 23.U) {
            state := sDummy
            bitCnt := 0.U
          }
        }
      }
    }

    is(sDummy) {  // Dummy 字节
      when(spiClkTick) {
        sclkReg := !sclkReg

        when(sclkReg) {
          bitCnt := bitCnt + 1.U
          when(bitCnt === 7.U) {
            state := sData
            bitCnt := 0.U
            shiftReg := 0.U
          }
        }
      }
    }

    is(sData) {  // 接收数据 (32 bits / 4 bytes)
      when(spiClkTick) {
        sclkReg := !sclkReg

        when(sclkReg) {
          // 上升沿：采样 MISO
          shiftReg := Cat(shiftReg(30, 0), misoSync)
          bitCnt := bitCnt + 1.U

          when(bitCnt === 31.U) {
            dataReg := Cat(shiftReg(30, 0), misoSync)
            readDone := true.B
            state := sIdle
          }
        }
      }
    }
  }

  // ============================================================
  // Wishbone 接口
  // ============================================================
  io.wb.dat_o := dataReg
  io.wb.ack_o := readDone

  // ============================================================
  // SPI 输出
  // ============================================================
  io.sclk := sclkReg
  io.mosi := mosiReg
  io.cs_n := csReg
}
