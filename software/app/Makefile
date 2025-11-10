# RV32E Application Makefile

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
LDFLAGS = -T../common/linker.ld -nostdlib -static

# Common files
COMMON_DIR = ../common
COMMON_OBJS = $(COMMON_DIR)/start.o $(COMMON_DIR)/soc.o $(COMMON_DIR)/libgcc.o

# Programs
PROGRAMS = hello blink fibonacci

# Targets
all: $(PROGRAMS)

$(COMMON_DIR)/start.o: $(COMMON_DIR)/start.S
	$(CC) $(ASFLAGS) -c $< -o $@

$(COMMON_DIR)/soc.o: $(COMMON_DIR)/soc.c $(COMMON_DIR)/soc.h
	$(CC) $(CFLAGS) -c $< -o $@

$(COMMON_DIR)/libgcc.o: $(COMMON_DIR)/libgcc.c
	$(CC) $(CFLAGS) -c $< -o $@

# Build each program
hello: hello.c $(COMMON_OBJS)
	$(CC) $(CFLAGS) $(LDFLAGS) $(COMMON_OBJS) $< -o $@.elf
	$(OBJCOPY) -O binary $@.elf $@.bin
	$(OBJDUMP) -D $@.elf > $@.dump
	$(SIZE) $@.elf

blink: blink.c $(COMMON_OBJS)
	$(CC) $(CFLAGS) $(LDFLAGS) $(COMMON_OBJS) $< -o $@.elf
	$(OBJCOPY) -O binary $@.elf $@.bin
	$(OBJDUMP) -D $@.elf > $@.dump
	$(SIZE) $@.elf

fibonacci: fibonacci.c $(COMMON_OBJS)
	$(CC) $(CFLAGS) $(LDFLAGS) $(COMMON_OBJS) $< -o $@.elf
	$(OBJCOPY) -O binary $@.elf $@.bin
	$(OBJDUMP) -D $@.elf > $@.dump
	$(SIZE) $@.elf

# Generate hex file for simulation
%.hex: %.bin
	hexdump -v -e '1/4 "%08x\n"' $< > $@

hex: $(PROGRAMS:=.hex)

# Clean
clean:
	rm -f $(COMMON_OBJS)
	rm -f *.elf *.bin *.dump *.hex

.PHONY: all hex clean
