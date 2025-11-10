#ifndef RT_CONFIG_H__
#define RT_CONFIG_H__

/* RT-Thread Nano Configuration for RV32E */

/* Basic configuration */
#define RT_NAME_MAX             8
#define RT_ALIGN_SIZE           4
#define RT_THREAD_PRIORITY_MAX  8
#define RT_TICK_PER_SECOND      100

/* Thread configuration */
#define IDLE_THREAD_STACK_SIZE  256
#define RT_USING_OVERFLOW_CHECK

/* Console configuration */
#define RT_USING_CONSOLE
#define RT_CONSOLEBUF_SIZE      128

/* Memory management */
#define RT_USING_SMALL_MEM
#define RT_USING_HEAP

/* No interrupt support */
#undef RT_USING_INTERRUPT

/* No system call support */
#undef RT_USING_SYSCALL

/* Debug configuration */
#ifdef RT_DEBUG
#define RT_DEBUG_INIT           1
#define RT_DEBUG_THREAD         1
#endif

#endif /* RT_CONFIG_H__ */
