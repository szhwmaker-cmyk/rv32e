#include "rtthread_stub.h"
#include "../common/soc.h"

/* Thread 1: LED blink simulation */
void thread1_entry(void *parameter) {
    static uint32_t count = 0;

    rt_kprintf("Thread 1: ");
    rt_kprintf("Count = ");

    // Print count
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
    rt_kprintf(buf);
    rt_kprintf("\n");

    count++;

    // Yield to other threads
    rt_thread_delay(100);
}

/* Thread 2: Simple counter */
void thread2_entry(void *parameter) {
    static uint32_t counter = 0;

    rt_kprintf("Thread 2: Counter = ");

    char buf[16];
    int idx = 0;
    uint32_t tmp = counter;
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
    rt_kprintf(buf);
    rt_kprintf("\n");

    counter += 2;

    rt_thread_delay(150);
}

int main(void) {
    // Initialize hardware
    uart_init();

    uart_puts("\n\n");
    uart_puts("====================================\n");
    uart_puts("  RV32E SoC - RT-Thread Test\n");
    uart_puts("====================================\n\n");

    uart_puts("RT-Thread Nano (Simplified)\n");
    uart_puts("No interrupt, no system call\n\n");

    // Initialize RT-Thread
    rt_system_init();

    // Create threads
    rt_thread_create("thread1", thread1_entry, NULL, 512, 1);
    rt_thread_create("thread2", thread2_entry, NULL, 512, 2);

    uart_puts("Threads created, starting scheduler...\n\n");

    // Start scheduler (never returns)
    rt_thread_run_all();

    return 0;
}
