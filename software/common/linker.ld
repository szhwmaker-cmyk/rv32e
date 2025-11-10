/* RV32E SoC Linker Script */

OUTPUT_ARCH("riscv")
ENTRY(_start)

MEMORY
{
  FLASH (rx)  : ORIGIN = 0x10000000, LENGTH = 256M
  RAM   (rwx) : ORIGIN = 0x80000000, LENGTH = 64K
}

SECTIONS
{
  /* Code section in Flash */
  .text : {
    *(.text.init)
    *(.text*)
    *(.rodata*)
  } > FLASH

  /* Data section in RAM */
  .data : AT(LOADADDR(.text) + SIZEOF(.text)) {
    _data_start = .;
    *(.data*)
    _data_end = .;
  } > RAM

  /* BSS section */
  .bss : {
    _bss_start = .;
    *(.bss*)
    *(COMMON)
    _bss_end = .;
  } > RAM

  /* Stack */
  .stack (NOLOAD) : {
    . = ALIGN(16);
    _stack_bottom = .;
    . += 4K;
    _stack_top = .;
  } > RAM

  _end = .;
}
