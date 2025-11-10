#include "../common/soc.h"

// 辅助函数：打印整数
void print_int(int num) {
    char buf[16];
    int idx = 0;

    if (num < 0) {
        uart_putc('-');
        num = -num;
    }

    if (num == 0) {
        buf[idx++] = '0';
    } else {
        int div = 1;
        while (num / div >= 10) div *= 10;
        while (div > 0) {
            buf[idx++] = '0' + (num / div);
            num %= div;
            div /= 10;
        }
    }
    buf[idx] = '\0';
    uart_puts(buf);
}

// 计算 Fibonacci 数列
uint32_t fibonacci(int n) {
    if (n <= 1) return n;

    uint32_t a = 0, b = 1, c;
    for (int i = 2; i <= n; i++) {
        c = a + b;
        a = b;
        b = c;
    }
    return b;
}

int main(void) {
    uart_init();

    uart_puts("\n");
    uart_puts("====================================\n");
    uart_puts("  RV32E SoC Test - Fibonacci\n");
    uart_puts("====================================\n\n");

    uart_puts("Calculating Fibonacci sequence...\n\n");

    for (int i = 0; i <= 15; i++) {
        uart_puts("fib(");
        print_int(i);
        uart_puts(") = ");
        print_int(fibonacci(i));
        uart_puts("\n");
    }

    uart_puts("\nFibonacci test completed!\n");

    while (1) {
        delay(1000000);
    }

    return 0;
}
