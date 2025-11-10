#include "../common/soc.h"

#define LED_PIN (1 << 0)

int main(void) {
    uart_init();

    uart_puts("\n");
    uart_puts("====================================\n");
    uart_puts("  RV32E SoC Test - LED Blink\n");
    uart_puts("====================================\n\n");

    // 配置 GPIO 为输出
    gpio_set_output(LED_PIN);

    uart_puts("LED blinking started...\n\n");

    uint32_t count = 0;
    while (1) {
        // LED ON
        gpio_write(LED_PIN);
        uart_puts("LED ON  - Count: ");

        // 简单的数字输出
        char buf[16];
        int idx = 0;
        uint32_t tmp = count;
        if (tmp == 0) {
            buf[idx++] = '0';
        } else {
            uint32_t div = 1;
            while (tmp / div >= 10) div *= 10;
            while (div > 0) {
                buf[idx++] = '0' + (tmp / div);
                tmp %= div;
                div /= 10;
            }
        }
        buf[idx] = '\0';
        uart_puts(buf);
        uart_puts("\n");

        delay(5000000);

        // LED OFF
        gpio_write(0);
        uart_puts("LED OFF\n");

        delay(5000000);

        count++;
    }

    return 0;
}
