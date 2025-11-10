/* Simple implementations of libgcc functions for RV32E
 *
 * These are needed because the standard libgcc.a is compiled for RV32I
 * and is incompatible with RV32E (different register counts).
 */

#include <stdint.h>

/* Unsigned integer division */
uint32_t __udivsi3(uint32_t a, uint32_t b) {
    if (b == 0) return 0;  // Division by zero

    uint32_t quotient = 0;
    uint32_t remainder = a;

    // Simple shift-and-subtract algorithm
    for (int i = 31; i >= 0; i--) {
        if (remainder >= (b << i)) {
            remainder -= (b << i);
            quotient |= (1U << i);
        }
    }

    return quotient;
}

/* Unsigned integer modulo */
uint32_t __umodsi3(uint32_t a, uint32_t b) {
    if (b == 0) return a;  // Division by zero

    uint32_t remainder = a;

    // Simple shift-and-subtract algorithm
    for (int i = 31; i >= 0; i--) {
        if (remainder >= (b << i)) {
            remainder -= (b << i);
        }
    }

    return remainder;
}

/* Signed integer division */
int32_t __divsi3(int32_t a, int32_t b) {
    if (b == 0) return 0;  // Division by zero

    int negative = 0;
    if (a < 0) {
        a = -a;
        negative = !negative;
    }
    if (b < 0) {
        b = -b;
        negative = !negative;
    }

    uint32_t result = __udivsi3((uint32_t)a, (uint32_t)b);
    return negative ? -(int32_t)result : (int32_t)result;
}

/* Signed integer modulo */
int32_t __modsi3(int32_t a, int32_t b) {
    if (b == 0) return a;  // Division by zero

    int negative = (a < 0);
    if (a < 0) a = -a;
    if (b < 0) b = -b;

    uint32_t result = __umodsi3((uint32_t)a, (uint32_t)b);
    return negative ? -(int32_t)result : (int32_t)result;
}
