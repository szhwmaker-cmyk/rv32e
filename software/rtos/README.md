# RT-Thread 移植说明

## 概述

本目录包含 RT-Thread 到 RV32E SoC 的移植代码。由于 RT-Thread 完整移植较为复杂，本项目提供简化版本，不包含中断和系统指令支持。

## RT-Thread 简介

RT-Thread 是一个开源的嵌入式实时操作系统，支持多种架构。

## 移植限制

由于架构文档要求，本移植有以下限制：
- **不支持中断**：所有中断相关功能被禁用
- **不支持系统指令**：不使用 CSR 指令
- **协作式调度**：使用协作式而非抢占式调度

## 目录结构

```
rtos/
├── rtconfig.h          # RT-Thread 配置文件
├── board.c             # 板级支持包
├── board.h             # 板级头文件
├── rtthread_stub.c     # RT-Thread 简化实现
├── main.c              # 主程序
├── Makefile            # 构建文件
└── README.md           # 本文件
```

## 构建方法

```bash
# 安装 RT-Thread
# 方法1：使用简化版本（本目录提供）
make

# 方法2：使用完整 RT-Thread（需要下载）
# git clone https://github.com/RT-Thread/rt-thread.git
# 然后根据移植指南配置
```

## 简化版 RT-Thread

本项目提供的简化版包含：
- 基础任务管理
- 简单的调度器
- 延迟函数
- 串口输出

## 完整移植步骤

如果要进行完整的 RT-Thread 移植：

### 1. 下载 RT-Thread 源码

```bash
git clone https://github.com/RT-Thread/rt-thread.git
```

### 2. 配置 RT-Thread

编辑 `rtconfig.h`：

```c
#define RT_USING_NANO           // 使用 Nano 版本
#define RT_TICK_PER_SECOND  100 // 每秒 100 tick
#define RT_ALIGN_SIZE       4   // 4 字节对齐
#define RT_THREAD_PRIORITY_MAX 8
#define RT_NAME_MAX         8
#define RT_USING_CONSOLE
#define RT_CONSOLEBUF_SIZE  128
```

### 3. 实现板级支持

需要实现以下函数：
- `rt_hw_board_init()` - 板级初始化
- `rt_hw_console_output()` - 控制台输出
- `rt_hw_cpu_icache_enable()` - I-Cache 使能（可选）
- `rt_hw_cpu_dcache_enable()` - D-Cache 使能（可选）

### 4. 实现上下文切换

由于不支持中断，需要实现协作式上下文切换：
- `rt_hw_context_switch()` - 上下文切换
- `rt_hw_context_switch_to()` - 切换到指定任务

### 5. 编译和烧录

```bash
make
# 将生成的 .bin 文件烧录到 SPI Flash
```

## 示例程序

见 `main.c` 中的示例，包含：
- 两个简单的任务
- 任务间通信
- 串口输出

## 注意事项

1. **内存限制**：SoC 只有 64KB RAM，需要谨慎使用内存
2. **堆栈大小**：每个任务的堆栈不宜过大
3. **无中断**：所有定时都基于轮询
4. **调试**：主要通过 UART 输出进行调试

## 参考资料

- [RT-Thread 官方文档](https://www.rt-thread.org/document/site/)
- [RT-Thread GitHub](https://github.com/RT-Thread/rt-thread)
- [RISC-V 移植指南](https://github.com/RT-Thread/rt-thread/tree/master/libcpu/risc-v)
