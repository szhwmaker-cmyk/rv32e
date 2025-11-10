#include "../common/soc.h"

int main(void) {
    // 初始化 UART
    uart_init();

    // 输出欢迎信息
    uart_puts("\n\n");
    uart_puts("====================================\n");
    uart_puts("  RV32E SoC Test - Hello World\n");
    uart_puts("====================================\n\n");

    uart_puts("Hello from RV32E processor!\n");
    uart_puts("System is running successfully.\n\n");

    // 测试基础指令
    uart_puts("Testing basic instructions...\n");

    int a = 10;
    int b = 20;
    int c = a + b;

    uart_puts("10 + 20 = ");
    // 简单的整数转字符串
    char buf[16];
    int idx = 0;
    int tmp = c;
    if (tmp == 0) {
        buf[idx++] = '0';
    } else {
        int div = 1;
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

    uart_puts("\nTest completed successfully!\n");

    // 无限循环
    while (1) {
        delay(1000000);
    }

    return 0;
}
