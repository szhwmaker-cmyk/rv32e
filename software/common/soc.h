#ifndef SOC_H
#define SOC_H

#include <stdint.h>

/* Memory Map */
#define SPI_FLASH_BASE  0x10000000
#define UART_BASE       0x20000000
#define GPIO_BASE       0x20010000
#define SPI_BASE        0x20020000
#define I2C_BASE        0x20030000
#define RAM_BASE        0x80000000

/* UART Registers */
#define UART_TXDATA     (*(volatile uint32_t*)(UART_BASE + 0x00))
#define UART_RXDATA     (*(volatile uint32_t*)(UART_BASE + 0x04))
#define UART_STATUS     (*(volatile uint32_t*)(UART_BASE + 0x08))
#define UART_BAUD       (*(volatile uint32_t*)(UART_BASE + 0x0C))
#define UART_CTRL       (*(volatile uint32_t*)(UART_BASE + 0x10))

/* UART Status Bits */
#define UART_TX_FULL    (1 << 7)
#define UART_TX_EMPTY   (1 << 6)
#define UART_RX_VALID   (1 << 5)
#define UART_TX_BUSY    (1 << 4)

/* GPIO Registers */
#define GPIO_DATA_IN    (*(volatile uint32_t*)(GPIO_BASE + 0x00))
#define GPIO_DATA_OUT   (*(volatile uint32_t*)(GPIO_BASE + 0x04))
#define GPIO_DIR        (*(volatile uint32_t*)(GPIO_BASE + 0x08))
#define GPIO_OE         (*(volatile uint32_t*)(GPIO_BASE + 0x0C))

/* Function Prototypes */
void uart_init(void);
void uart_putc(char c);
void uart_puts(const char* s);
char uart_getc(void);
int uart_rx_available(void);

void gpio_set_output(uint32_t pins);
void gpio_set_input(uint32_t pins);
void gpio_write(uint32_t value);
uint32_t gpio_read(void);

void delay(uint32_t cycles);

#endif /* SOC_H */
