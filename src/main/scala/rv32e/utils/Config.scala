package rv32e.utils

import chisel3._

/**
 * RV32E SoC 配置参数
 */
object Config {
  // 系统参数
  val XLEN = 32                    // 数据宽度
  val NUM_REGS = 16                // RV32E 寄存器数量

  // 地址空间定义
  val SPI_FLASH_BASE = 0x10000000L // SPI Flash 基地址
  val SPI_FLASH_SIZE = 0x10000000L // SPI Flash 大小 (256MB)
  val SPI_FLASH_END  = SPI_FLASH_BASE + SPI_FLASH_SIZE - 1

  val UART_BASE      = 0x20000000L // UART 基地址
  val UART_SIZE      = 0x00010000L // UART 大小 (64KB)
  val UART_END       = UART_BASE + UART_SIZE - 1

  val GPIO_BASE      = 0x20010000L // GPIO 基地址
  val GPIO_SIZE      = 0x00010000L // GPIO 大小 (64KB)
  val GPIO_END       = GPIO_BASE + GPIO_SIZE - 1

  val SPI_BASE       = 0x20020000L // SPI Master 基地址
  val SPI_SIZE       = 0x00010000L // SPI Master 大小 (64KB)
  val SPI_END        = SPI_BASE + SPI_SIZE - 1

  val I2C_BASE       = 0x20030000L // I2C Master 基地址
  val I2C_SIZE       = 0x00010000L // I2C Master 大小 (64KB)
  val I2C_END        = I2C_BASE + I2C_SIZE - 1

  val RAM_BASE       = 0x80000000L // RAM 基地址
  val RAM_SIZE       = 0x10000000L // RAM 大小 (256MB, 实际实现可能更小)
  val RAM_END        = RAM_BASE + RAM_SIZE - 1

  // 启动地址
  val RESET_VECTOR   = SPI_FLASH_BASE  // 复位后从 SPI Flash 启动

  // 时钟参数
  val CLK_FREQ       = 50000000    // 系统时钟频率 50MHz
  val UART_BAUD      = 115200      // UART 波特率
  val UART_DIV       = CLK_FREQ / UART_BAUD  // UART 分频值 (约 434)

  val SPI_CLK_DIV    = 4           // SPI 时钟分频 (50MHz / 4 = 12.5MHz)
  val I2C_CLK_DIV    = 500         // I2C 时钟分频 (50MHz / 500 = 100kHz)
}

/**
 * UART 寄存器偏移
 */
object UARTRegs {
  val TXDATA = 0x00  // 发送数据寄存器
  val RXDATA = 0x04  // 接收数据寄存器
  val STATUS = 0x08  // 状态寄存器
  val BAUD   = 0x0C  // 波特率分频值
  val CTRL   = 0x10  // 控制寄存器
}

/**
 * GPIO 寄存器偏移
 */
object GPIORegs {
  val DATA_IN  = 0x00  // 输入数据
  val DATA_OUT = 0x04  // 输出数据
  val DIR      = 0x08  // 方向控制
  val OE       = 0x0C  // 输出使能
}

/**
 * SPI 寄存器偏移
 */
object SPIRegs {
  val CTRL   = 0x00  // 控制寄存器
  val DIV    = 0x04  // 时钟分频
  val TXDATA = 0x08  // 发送数据
  val RXDATA = 0x0C  // 接收数据
  val STATUS = 0x10  // 状态寄存器
}

/**
 * I2C 寄存器偏移
 */
object I2CRegs {
  val CTRL   = 0x00  // 控制寄存器
  val DIV    = 0x04  // 时钟分频
  val ADDR   = 0x08  // 从设备地址
  val TXDATA = 0x0C  // 发送数据
  val RXDATA = 0x10  // 接收数据
  val STATUS = 0x14  // 状态寄存器
  val CMD    = 0x18  // 命令寄存器
}
