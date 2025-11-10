# RV32E 常见编译错误及解决方案

## 错误 1: NULL 未定义

### 症状
```
error: 'NULL' undeclared (first use in this function)
```

### 原因
使用了 `NULL` 但没有包含定义它的头文件

### 解决方案
在文件开头添加：
```c
#include <stddef.h>
```

---

## 错误 2: 链接时未定义的除法/取模函数

### 症状
```
undefined reference to `__udivsi3'
undefined reference to `__umodsi3'
undefined reference to `__modsi3'
```

### 原因
RV32E 使用 `-nostdlib` 编译，但除法/取模操作需要 libgcc 提供的软件实现函数

### 错误做法 ❌
```makefile
LDFLAGS = -T../common/linker.ld -nostdlib -static -lgcc  # 库在前面
$(TARGET).elf: $(OBJS)
	$(CC) $(CFLAGS) $(LDFLAGS) $^ -o $@
```

### 正确做法 ✅
```makefile
LDFLAGS = -T../common/linker.ld -nostdlib -static  # LDFLAGS 中不含 -lgcc
$(TARGET).elf: $(OBJS)
	$(CC) $(CFLAGS) $(LDFLAGS) $^ -lgcc -o $@  # -lgcc 放在目标文件之后
```

### 关键原则
**Unix 链接器规则：库必须放在引用它们的目标文件之后**

链接顺序：
```
gcc [选项] [目标文件] -l[库名] -o [输出]
         ↑           ↑
       先写        后写
```

---

## 其他提示

### 未使用参数警告
如果函数参数未使用，添加：
```c
void func(void *parameter) {
    (void)parameter;  /* Unused */
    // ...
}
```
