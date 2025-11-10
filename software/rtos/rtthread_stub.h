#ifndef RTTHREAD_STUB_H
#define RTTHREAD_STUB_H

#include <stdint.h>

/* RT-Thread stub API */

void rt_system_init(void);

int rt_thread_create(const char *name,
                     void (*entry)(void *parameter),
                     void *parameter,
                     uint32_t stack_size,
                     uint8_t priority);

void rt_thread_run_all(void);
void rt_thread_yield(void);
void rt_thread_delay(uint32_t tick);

uint32_t rt_tick_get(void);

void rt_kputs(const char *str);
void rt_kprintf(const char *fmt, ...);

#endif /* RTTHREAD_STUB_H */
