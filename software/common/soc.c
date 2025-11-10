#include "soc.h"

/* UART Functions */
void uart_init(void) {
    // 波特率已在硬件中设置为默认值
    UART_CTRL = 0x03; // Enable TX and RX
}

void uart_putc(char c) {
    // 等待 TX FIFO 不满
    while (UART_STATUS & UART_TX_FULL);
    UART_TXDATA = c;
}

void uart_puts(const char* s) {
    while (*s) {
        if (*s == '\n') {
            uart_putc('\r');
        }
        uart_putc(*s++);
    }
}

char uart_getc(void) {
    // 等待接收数据
    while (!(UART_STATUS & UART_RX_VALID));
    return (char)UART_RXDATA;
}

int uart_rx_available(void) {
    return (UART_STATUS & UART_RX_VALID) ? 1 : 0;
}

/* GPIO Functions */
void gpio_set_output(uint32_t pins) {
    GPIO_DIR |= pins;
    GPIO_OE  |= pins;
}

void gpio_set_input(uint32_t pins) {
    GPIO_DIR &= ~pins;
    GPIO_OE  &= ~pins;
}

void gpio_write(uint32_t value) {
    GPIO_DATA_OUT = value;
}

uint32_t gpio_read(void) {
    return GPIO_DATA_IN;
}

/* Simple delay function */
void delay(uint32_t cycles) {
    for (volatile uint32_t i = 0; i < cycles; i++) {
        __asm__ volatile ("nop");
    }
}
