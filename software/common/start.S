# RV32E Startup Code
# 从 SPI Flash 启动，初始化堆栈并跳转到 main

.section .text.init
.globl _start
.type _start, @function

_start:
    # 初始化全局指针 (RV32E 使用 x0-x15)
    # x0 恒为 0
    # x2 (sp) 用作堆栈指针

    # 设置堆栈指针
    lui  x2, %hi(_stack_top)
    addi x2, x2, %lo(_stack_top)

    # 清零 BSS 段
    lui  x10, %hi(_bss_start)
    addi x10, x10, %lo(_bss_start)
    lui  x11, %hi(_bss_end)
    addi x11, x11, %lo(_bss_end)

clear_bss:
    # 如果 _bss_start >= _bss_end，跳过
    sub  x12, x11, x10
    beq  x12, x0, bss_done

    # 清零一个字
    sw   x0, 0(x10)
    addi x10, x10, 4
    j    clear_bss

bss_done:
    # 调用 main 函数
    jal  x1, main

    # 如果 main 返回，进入无限循环
halt:
    j halt

.size _start, .-_start
