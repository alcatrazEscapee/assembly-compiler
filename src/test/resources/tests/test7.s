# Generated by Assembly Auto-Compiler by Alex O'Neill
# Setup
    .equ            LAST_RAM_WORD, 0x007FFFFC
    .global         _start
    .org            0x00000000
    .text

# Entry point
_start:
    movia           sp, LAST_RAM_WORD
    ldw             r2, A(r0)
    ldw             r3, B(r0)
    call            AddValues
    stw             r2, C(r0)
_end:
    br              _end

# ========== AddValues ==========
AddValues:
    subi            sp, sp, 4
    stw             r16, 0(sp)

    add             r16, r2, r3
    mov             r2, r16

    ldw             r16, 0(sp)
    addi            sp, sp, 4
    ret

# Word-Aligned Variables
    .org            0x00001000

A:
    .word           3
B:
    .word           5
C:
    .skip           4

# End of Assembly Source
    .end