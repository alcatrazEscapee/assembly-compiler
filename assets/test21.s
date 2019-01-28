# Generated by Assembly Auto-Compiler by Alex O'Neill
# Setup
    .equ            LAST_RAM_WORD, 0x007FFFFC
    .global         _start
    .org            0x00000000
    .text

# Entry point
_start:
    movia           sp, LAST_RAM_WORD
    movi            r2, 1
    ldw             r3, x(r0)
    call            Factorial
_end:
    br              _end

# ========== Factorial ==========
Factorial:
    subi            sp, sp, 8
    stw             r3, 4(sp)
    stw             ra, 0(sp)

    bne             r3, r0, f_if1
    br              f_ret
f_if1:
    mul             r2, r2, r3
    subi            r3, r3, 1
    call            Factorial
f_ret:

    ldw             r3, 4(sp)
    ldw             ra, 0(sp)
    addi            sp, sp, 8
    ret

# Word-Aligned Variables
    .org            0x00001000

x:
    .word           10

# End of Assembly Source
    .end