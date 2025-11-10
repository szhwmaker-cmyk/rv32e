/**
 * RT-Thread Simplified Implementation
 * For RV32E without interrupt and system instruction support
 */

#include "rtthread_stub.h"
#include "../common/soc.h"
#include <stdint.h>
#include <stddef.h>

/* Simple task structure */
typedef struct rt_thread {
    char name[8];
    void (*entry)(void *parameter);
    void *parameter;
    uint8_t priority;
    uint32_t *stack_ptr;
    uint32_t stack_size;
    uint8_t stat;
} rt_thread_t;

#define RT_THREAD_READY   0
#define RT_THREAD_SUSPEND 1

#define MAX_THREADS 4

static rt_thread_t threads[MAX_THREADS];
static int thread_count = 0;
static int current_thread = 0;
static uint32_t tick_count = 0;

/* Initialize RT-Thread */
void rt_system_init(void) {
    thread_count = 0;
    current_thread = 0;
    tick_count = 0;

    uart_puts("RT-Thread stub initialized\n");
}

/* Create a thread */
int rt_thread_create(const char *name,
                     void (*entry)(void *parameter),
                     void *parameter,
                     uint32_t stack_size,
                     uint8_t priority) {
    if (thread_count >= MAX_THREADS) {
        return -1;
    }

    rt_thread_t *thread = &threads[thread_count];

    // Copy name
    int i;
    for (i = 0; i < 7 && name[i]; i++) {
        thread->name[i] = name[i];
    }
    thread->name[i] = '\0';

    thread->entry = entry;
    thread->parameter = parameter;
    thread->priority = priority;
    thread->stack_size = stack_size;
    thread->stat = RT_THREAD_READY;

    thread_count++;
    return thread_count - 1;
}

/* Simple cooperative scheduler */
void rt_schedule(void) {
    // Find next ready thread
    int next = (current_thread + 1) % thread_count;
    while (next != current_thread && threads[next].stat != RT_THREAD_READY) {
        next = (next + 1) % thread_count;
    }

    if (threads[next].stat == RT_THREAD_READY) {
        current_thread = next;
    }
}

/* Run all threads (cooperative) */
void rt_thread_run_all(void) {
    uart_puts("Starting all threads...\n");

    while (1) {
        for (int i = 0; i < thread_count; i++) {
            if (threads[i].stat == RT_THREAD_READY) {
                // Run thread entry (cooperative, thread must yield)
                threads[i].entry(threads[i].parameter);
            }
        }

        tick_count++;

        // Simple delay to simulate tick
        delay(50000);
    }
}

/* Thread yield */
void rt_thread_yield(void) {
    rt_schedule();
}

/* Thread delay */
void rt_thread_delay(uint32_t tick) {
    uint32_t start = tick_count;
    while ((tick_count - start) < tick) {
        delay(1000);
    }
}

/* Get current tick */
uint32_t rt_tick_get(void) {
    return tick_count;
}

/* Console output */
void rt_kputs(const char *str) {
    uart_puts(str);
}

void rt_kprintf(const char *fmt, ...) {
    // Simple implementation - just print the format string
    uart_puts(fmt);
}
