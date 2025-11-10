# RT-Thread for RV32E Makefile

# Toolchain
PREFIX = riscv32-unknown-elf-
CC = $(PREFIX)gcc
AS = $(PREFIX)as
LD = $(PREFIX)ld
OBJCOPY = $(PREFIX)objcopy
OBJDUMP = $(PREFIX)objdump
SIZE = $(PREFIX)size

# Flags
ARCH = rv32e
ABI = ilp32e
CFLAGS = -march=$(ARCH) -mabi=$(ABI) -O2 -Wall -Wextra
CFLAGS += -fno-builtin -nostdlib -nostartfiles
CFLAGS += -ffreestanding -fno-common
ASFLAGS = -march=$(ARCH) -mabi=$(ABI)
LDFLAGS = -T../common/linker.ld -nostdlib -static -lgcc

# Source files
COMMON_DIR = ../common
SRCS = main.c rtthread_stub.c
SRCS += $(COMMON_DIR)/soc.c
OBJS = $(SRCS:.c=.o)
OBJS += $(COMMON_DIR)/start.o

# Output
TARGET = rtos

# Build rules
all: $(TARGET).bin

$(COMMON_DIR)/start.o: $(COMMON_DIR)/start.S
	$(CC) $(ASFLAGS) -c $< -o $@

%.o: %.c
	$(CC) $(CFLAGS) -c $< -o $@

$(TARGET).elf: $(OBJS)
	$(CC) $(CFLAGS) $(LDFLAGS) $^ -o $@

$(TARGET).bin: $(TARGET).elf
	$(OBJCOPY) -O binary $< $@
	$(OBJDUMP) -D $< > $(TARGET).dump
	$(SIZE) $<

$(TARGET).hex: $(TARGET).bin
	hexdump -v -e '1/4 "%08x\n"' $< > $@

hex: $(TARGET).hex

clean:
	rm -f *.o $(COMMON_DIR)/*.o
	rm -f $(TARGET).elf $(TARGET).bin $(TARGET).dump $(TARGET).hex

.PHONY: all hex clean
