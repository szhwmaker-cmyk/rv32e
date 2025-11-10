# RV32E SoC Application Programs

## 概述

本目录包含 RV32E SoC 的应用程序测试代码。

## 测试程序

1. **hello** - Hello World 程序
   - 测试基本的 UART 输出
   - 测试基础算术指令

2. **blink** - LED 闪烁程序
   - 测试 GPIO 输出控制
   - 测试定时延迟功能

3. **fibonacci** - Fibonacci 数列计算
   - 测试循环和算术运算
   - 测试函数调用

## 构建要求

需要安装 RISC-V 工具链：

```bash
# Ubuntu/Debian
sudo apt-get install gcc-riscv64-unknown-elf

# 或从源码构建
git clone https://github.com/riscv/riscv-gnu-toolchain
cd riscv-gnu-toolchain
./configure --prefix=/opt/riscv --with-arch=rv32e --with-abi=ilp32e
make
```

## 构建方法

```bash
# 构建所有程序
make all

# 构建特定程序
make hello
make blink
make fibonacci

# 生成十六进制文件（用于仿真）
make hex

# 清理
make clean
```

## 输出文件

每个程序会生成以下文件：
- `*.elf` - ELF 可执行文件
- `*.bin` - 二进制映像（用于烧录到 SPI Flash）
- `*.dump` - 反汇编文件（用于调试）
- `*.hex` - 十六进制文件（用于仿真）

## 烧录到 SPI Flash

使用 SPI Flash 编程器将 `.bin` 文件烧录到地址 0x00000000 开始的位置。

## 仿真运行

将生成的 `.bin` 或 `.hex` 文件加载到仿真器的 SPI Flash 模型中，然后启动 SoC 仿真。

## 内存映射

- SPI Flash: 0x10000000 - 0x1FFFFFFF (程序代码)
- UART:      0x20000000 - 0x2000FFFF
- GPIO:      0x20010000 - 0x2001FFFF
- RAM:       0x80000000 - 0x8FFFFFFF (数据和堆栈)
