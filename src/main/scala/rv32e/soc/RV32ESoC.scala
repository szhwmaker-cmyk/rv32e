package rv32e.soc

import chisel3._
import chisel3.util._
import rv32e.core._
import rv32e.bus._
import rv32e.peripheral._

/**
 * RV32E SoC 顶层模块
 * 集成处理器核心、总线互联和所有外设
 */
class RV32ESoC extends Module {
  val io = IO(new Bundle {
    // UART 接口
    val uart_tx = Output(Bool())
    val uart_rx = Input(Bool())

    // GPIO 接口
    val gpio_in  = Input(UInt(32.W))
    val gpio_out = Output(UInt(32.W))
    val gpio_oe  = Output(UInt(32.W))

    // SPI Master 接口
    val spi_sclk = Output(Bool())
    val spi_mosi = Output(Bool())
    val spi_miso = Input(Bool())
    val spi_cs_n = Output(Bool())

    // I2C Master 接口
    val i2c_scl_out = Output(Bool())
    val i2c_scl_in  = Input(Bool())
    val i2c_sda_out = Output(Bool())
    val i2c_sda_in  = Input(Bool())

    // SPI Flash 接口
    val flash_sclk = Output(Bool())
    val flash_mosi = Output(Bool())
    val flash_miso = Input(Bool())
    val flash_cs_n = Output(Bool())
  })

  // ============================================================
  // 模块实例化
  // ============================================================

  // 处理器核心
  val core = Module(new Core)

  // 总线互联
  val interconnect = Module(new WishboneInterconnect)

  // 外设控制器
  val spiFlashCtrl = Module(new SPIFlashController)
  val uartCtrl     = Module(new UARTController)
  val gpioCtrl     = Module(new GPIOController(32))
  val spiCtrl      = Module(new SPIMaster)
  val i2cCtrl      = Module(new I2CMaster)
  val ramCtrl      = Module(new RAMController(64 * 1024))

  // ============================================================
  // 指令和数据总线仲裁
  // ============================================================
  val busArbiter = Module(new WishboneArbiter(2))

  // 连接核心的指令和数据端口到仲裁器
  busArbiter.io.masters(0) <> core.io.imem
  busArbiter.io.masters(1) <> core.io.dmem

  // 仲裁器输出连接到互联
  interconnect.io.master <> busArbiter.io.slave

  // ============================================================
  // 外设连接
  // ============================================================

  // SPI Flash
  Wishbone.connect(interconnect.io.spiFlash, spiFlashCtrl.io.wb)
  io.flash_sclk := spiFlashCtrl.io.sclk
  io.flash_mosi := spiFlashCtrl.io.mosi
  spiFlashCtrl.io.miso := io.flash_miso
  io.flash_cs_n := spiFlashCtrl.io.cs_n

  // UART
  Wishbone.connect(interconnect.io.uart, uartCtrl.io.wb)
  io.uart_tx := uartCtrl.io.tx
  uartCtrl.io.rx := io.uart_rx

  // GPIO
  Wishbone.connect(interconnect.io.gpio, gpioCtrl.io.wb)
  gpioCtrl.io.pins_in := io.gpio_in
  io.gpio_out := gpioCtrl.io.pins_out
  io.gpio_oe  := gpioCtrl.io.pins_oe

  // SPI Master
  Wishbone.connect(interconnect.io.spi, spiCtrl.io.wb)
  io.spi_sclk := spiCtrl.io.sclk
  io.spi_mosi := spiCtrl.io.mosi
  spiCtrl.io.miso := io.spi_miso
  io.spi_cs_n := spiCtrl.io.cs_n

  // I2C Master
  Wishbone.connect(interconnect.io.i2c, i2cCtrl.io.wb)
  io.i2c_scl_out := i2cCtrl.io.scl_out
  i2cCtrl.io.scl_in := io.i2c_scl_in
  io.i2c_sda_out := i2cCtrl.io.sda_out
  i2cCtrl.io.sda_in := io.i2c_sda_in

  // RAM
  Wishbone.connect(interconnect.io.ram, ramCtrl.io.wb)
}

/**
 * RV32E SoC 生成器
 * 用于生成 Verilog 代码
 */
object RV32ESoC extends App {
  (new chisel3.stage.ChiselStage).emitVerilog(
    new RV32ESoC,
    Array("--target-dir", "generated")
  )
}
