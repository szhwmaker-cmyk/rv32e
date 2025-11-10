# RV32E SoC - RISC-V 五级流水线处理器系统

## 项目概述

本项目实现了一个完整的基于 RV32E 指令集的五级流水线处理器 SoC 系统。系统包含处理器核心、总线互联和完整的外设子系统，支持从 SPI Flash 启动应用程序和简单的 RT-Thread 操作系统。

## 主要特性

### 处理器核心
- **架构**: RISC-V RV32E（16 个通用寄存器）
- **流水线**: 五级流水线（IF → ID → EX → MEM → WB）
- **指令集**: RV32E 基础整数指令集（37 条指令）
- **冒险处理**:
  - 数据冒险转发
  - LOAD-USE 检测和暂停
  - 分支预测（静态不跳转）

### 总线系统
- **协议**: Wishbone B4 Pipeline
- **拓扑**: 共享总线 + 地址译码
- **主设备**: 处理器核心（指令和数据总线）
- **从设备**: SPI Flash, UART, GPIO, SPI, I2C, RAM

### 外设系统
- **UART**: 可配置波特率串口（默认 115200 bps）
- **GPIO**: 32 位通用 I/O
- **SPI Master**: SPI 主控制器
- **I2C Master**: I2C 主控制器
- **SPI Flash**: 支持 Fast Read 的 Flash 控制器

### 存储系统
- **SPI Flash**: 256MB 地址空间（启动代码）
- **RAM**: 64KB 片上 SRAM

## 目录结构

```
rv32e/
├── src/
│   ├── main/scala/rv32e/
│   │   ├── core/          # 处理器核心
│   │   │   ├── ALU.scala
│   │   │   ├── RegFile.scala
│   │   │   ├── Decoder.scala
│   │   │   ├── Core.scala
│   │   │   └── ...
│   │   ├── bus/           # 总线系统
│   │   │   ├── WishboneBundle.scala
│   │   │   └── WishboneInterconnect.scala
│   │   ├── peripheral/    # 外设控制器
│   │   │   ├── UARTController.scala
│   │   │   ├── GPIOController.scala
│   │   │   ├── SPIMaster.scala
│   │   │   ├── I2CMaster.scala
│   │   │   ├── SPIFlashController.scala
│   │   │   └── RAMController.scala
│   │   ├── soc/           # SoC 集成
│   │   │   └── RV32ESoC.scala
│   │   └── utils/         # 工具类
│   │       └── Config.scala
│   └── test/scala/rv32e/  # 测试文件
├── sim/
│   └── models/            # 仿真模型
│       ├── UARTModel.scala
│       └── SPIFlashModel.scala
├── software/
│   ├── common/            # 公共代码
│   │   ├── linker.ld      # 链接脚本
│   │   ├── start.S        # 启动代码
│   │   ├── soc.h          # HAL 头文件
│   │   └── soc.c          # HAL 实现
│   ├── app/               # 应用程序
│   │   ├── hello.c        # Hello World
│   │   ├── blink.c        # LED 闪烁
│   │   ├── fibonacci.c    # Fibonacci 计算
│   │   ├── Makefile
│   │   └── README.md
│   └── rtos/              # RT-Thread
│       ├── main.c
│       ├── rtthread_stub.c
│       ├── Makefile
│       └── README.md
├── build.sbt              # Chisel 构建文件
└── README.md              # 本文件
```

## 快速开始

### 环境准备

1. **安装 Chisel 开发环境**:
```bash
# 安装 SBT (Scala Build Tool)
echo "deb https://repo.scala-sbt.org/scalasbt/debian all main" | sudo tee /etc/apt/sources.list.d/sbt.list
curl -sL "https://keyserver.ubuntu.com/pks/lookup?op=get&search=0x2EE0EA64E40A89B84B2DF73499E82A75642AC823" | sudo apt-key add
sudo apt-get update
sudo apt-get install sbt

# 安装 Java JDK
sudo apt-get install openjdk-11-jdk
```

2. **安装 RISC-V 工具链**:
```bash
sudo apt-get install gcc-riscv64-unknown-elf

# 或从源码构建
git clone https://github.com/riscv/riscv-gnu-toolchain
cd riscv-gnu-toolchain
./configure --prefix=/opt/riscv --with-arch=rv32e --with-abi=ilp32e
make
```

### 构建硬件

```bash
# 编译 Chisel 代码
sbt compile
# 或使用 mill
mill chisel.compile

# 生成 Verilog 代码
sbt "runMain rv32e.soc.VerilogGenerator" > generated/RV32ESoC.v
# 或使用 mill
mill chisel.runMain rv32e.soc.VerilogGenerator > generated/RV32ESoC.v

# 运行单元测试
sbt test
# 或使用 mill
mill chisel.test

# 运行特定测试
sbt "testOnly rv32e.peripheral.UARTTest"
```

### 构建软件

```bash
# 构建应用程序
cd software/app
make all

# 构建 RT-Thread
cd software/rtos
make
```

## 地址空间映射

| 设备 | 基地址 | 结束地址 | 大小 |
|------|--------|----------|------|
| SPI Flash | 0x10000000 | 0x1FFFFFFF | 256 MB |
| UART | 0x20000000 | 0x2000FFFF | 64 KB |
| GPIO | 0x20010000 | 0x2001FFFF | 64 KB |
| SPI Master | 0x20020000 | 0x2002FFFF | 64 KB |
| I2C Master | 0x20030000 | 0x2003FFFF | 64 KB |
| RAM | 0x80000000 | 0x8FFFFFFF | 256 MB (实际 64KB) |

## 启动流程

1. 处理器从 SPI Flash 地址 0x10000000 开始执行
2. 启动代码初始化堆栈指针
3. 清零 BSS 段
4. 跳转到 main 函数
5. 应用程序开始运行

## 测试程序

### 应用程序测试

1. **Hello World** (`software/app/hello.c`)
   - 测试 UART 输出
   - 测试基础算术运算

2. **LED Blink** (`software/app/blink.c`)
   - 测试 GPIO 控制
   - 测试延迟功能

3. **Fibonacci** (`software/app/fibonacci.c`)
   - 测试循环和函数调用
   - 测试算术运算

### RT-Thread 测试

简化的 RT-Thread 移植，支持：
- 基础任务管理
- 协作式调度
- 无中断模式
- 串口输出

运行方式：
```bash
cd software/rtos
make
# 将 rtos.bin 烧录到 SPI Flash
```

## 性能指标

- **时钟频率**: 50 MHz（目标）
- **理论 CPI**: 1.0
- **实际 CPI**: ~1.35（考虑冒险）
- **启动时间**: ~43 ms（64KB 程序从 Flash 加载）

## 开发工具

- **硬件开发**: Chisel 3.6.0
- **仿真**: ChiselTest 0.6.0
- **软件开发**: RISC-V GCC
- **调试**: UART 串口输出

## 文档

- [架构设计文档](docs/architecture.md)（原始需求文档）
- [应用程序开发指南](software/app/README.md)
- [RT-Thread 移植说明](software/rtos/README.md)

## 许可证

本项目采用 MIT 许可证。

## 贡献

欢迎提交 Issue 和 Pull Request！

## 联系方式

如有问题或建议，请通过 GitHub Issues 联系。

---

**注意**: 本项目为教学和研究目的开发，适用于嵌入式系统学习和 RISC-V 架构研究。
