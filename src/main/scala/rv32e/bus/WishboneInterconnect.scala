package rv32e.bus

import chisel3._
import chisel3.util._
import rv32e.utils.Config

/**
 * Wishbone 互联 (Crossbar)
 * 连接一个主设备到多个从设备
 * 通过地址译码选择目标从设备
 */
class WishboneInterconnect extends Module {
  val io = IO(new Bundle {
    // 主设备接口
    val master = Flipped(new WishboneMaster)

    // 从设备接口
    val spiFlash = new WishboneMaster
    val uart     = new WishboneMaster
    val gpio     = new WishboneMaster
    val spi      = new WishboneMaster
    val i2c      = new WishboneMaster
    val ram      = new WishboneMaster
  })

  // 地址译码
  val addr = io.master.adr_o

  val selSpiFlash = addr >= Config.SPI_FLASH_BASE.U && addr <= Config.SPI_FLASH_END.U
  val selUart     = addr >= Config.UART_BASE.U && addr <= Config.UART_END.U
  val selGpio     = addr >= Config.GPIO_BASE.U && addr <= Config.GPIO_END.U
  val selSpi      = addr >= Config.SPI_BASE.U && addr <= Config.SPI_END.U
  val selI2c      = addr >= Config.I2C_BASE.U && addr <= Config.I2C_END.U
  val selRam      = addr >= Config.RAM_BASE.U && addr <= Config.RAM_END.U

  // 从设备选择编码
  val slaveSelect = MuxCase(0.U, Seq(
    selSpiFlash -> 1.U,
    selUart     -> 2.U,
    selGpio     -> 3.U,
    selSpi      -> 4.U,
    selI2c      -> 5.U,
    selRam      -> 6.U
  ))

  // 默认所有从设备为空闲
  Wishbone.idleMaster(io.spiFlash)
  Wishbone.idleMaster(io.uart)
  Wishbone.idleMaster(io.gpio)
  Wishbone.idleMaster(io.spi)
  Wishbone.idleMaster(io.i2c)
  Wishbone.idleMaster(io.ram)

  // 根据选择连接对应的从设备
  val selectedSlave = WireDefault(new WishboneMaster)
  Wishbone.idleMaster(selectedSlave)

  switch(slaveSelect) {
    is(1.U) {
      io.spiFlash <> io.master
      selectedSlave.dat_i := io.spiFlash.dat_i
      selectedSlave.ack_i := io.spiFlash.ack_i
    }
    is(2.U) {
      io.uart <> io.master
      selectedSlave.dat_i := io.uart.dat_i
      selectedSlave.ack_i := io.uart.ack_i
    }
    is(3.U) {
      io.gpio <> io.master
      selectedSlave.dat_i := io.gpio.dat_i
      selectedSlave.ack_i := io.gpio.ack_i
    }
    is(4.U) {
      io.spi <> io.master
      selectedSlave.dat_i := io.spi.dat_i
      selectedSlave.ack_i := io.spi.ack_i
    }
    is(5.U) {
      io.i2c <> io.master
      selectedSlave.dat_i := io.i2c.dat_i
      selectedSlave.ack_i := io.i2c.ack_i
    }
    is(6.U) {
      io.ram <> io.master
      selectedSlave.dat_i := io.ram.dat_i
      selectedSlave.ack_i := io.ram.ack_i
    }
  }

  // 主设备输入信号（来自从设备）
  io.master.dat_i := selectedSlave.dat_i
  io.master.ack_i := selectedSlave.ack_i
}

/**
 * 简化的 Wishbone Arbiter
 * 用于多主设备仲裁（当前实现为单主设备，预留扩展）
 */
class WishboneArbiter(numMasters: Int = 2) extends Module {
  val io = IO(new Bundle {
    val masters = Vec(numMasters, Flipped(new WishboneMaster))
    val slave   = new WishboneMaster
  })

  // 简单的优先级仲裁
  val requests = VecInit(io.masters.map(m => m.cyc_o && m.stb_o))
  val grant = PriorityEncoder(requests)

  // 连接被授权的主设备
  io.slave <> io.masters(grant)

  // 将应答路由回对应的主设备
  for (i <- 0 until numMasters) {
    when(grant === i.U) {
      io.masters(i).dat_i := io.slave.dat_i
      io.masters(i).ack_i := io.slave.ack_i
    }.otherwise {
      io.masters(i).dat_i := 0.U
      io.masters(i).ack_i := false.B
    }
  }
}
