# RV32E SoC Top-Level Makefile

.PHONY: all hardware software test clean help

# Default target
all: help

# Generate Verilog from Chisel
hardware:
	@echo "Generating Verilog from Chisel..."
	sbt "runMain rv32e.soc.RV32ESoC"

# Run hardware tests
test:
	@echo "Running hardware tests..."
	sbt test

# Build all software
software:
	@echo "Building application software..."
	cd software/app && $(MAKE) all
	@echo "Building RT-Thread..."
	cd software/rtos && $(MAKE) all

# Build only applications
app:
	@echo "Building applications..."
	cd software/app && $(MAKE) all

# Build only RT-Thread
rtos:
	@echo "Building RT-Thread..."
	cd software/rtos && $(MAKE) all

# Clean all build artifacts
clean:
	@echo "Cleaning hardware..."
	rm -rf generated/ target/ project/target/
	@echo "Cleaning software..."
	cd software/app && $(MAKE) clean
	cd software/rtos && $(MAKE) clean

# Help message
help:
	@echo "RV32E SoC Build System"
	@echo "======================"
	@echo ""
	@echo "Available targets:"
	@echo "  hardware  - Generate Verilog from Chisel sources"
	@echo "  test      - Run hardware unit tests"
	@echo "  software  - Build all software (apps + RTOS)"
	@echo "  app       - Build application programs only"
	@echo "  rtos      - Build RT-Thread only"
	@echo "  clean     - Clean all build artifacts"
	@echo "  help      - Show this help message"
	@echo ""
	@echo "Quick start:"
	@echo "  1. make hardware    # Generate Verilog"
	@echo "  2. make test        # Run tests"
	@echo "  3. make software    # Build software"
