# Generated by Assembly Auto-Compiler by Alex O'Neill
# Setup
    .equ            LAST_RAM_WORD, 0x007FFFFC
    .global         _start
    .org            0x00000000
    .text

# Entry point
_start:
    movia           sp, LAST_RAM_WORD
    ble             r2, r0, main_if1
    movi            r3, 1
    br              main_else1
main_if1:
    movi            r3, 2
main_else1:
_end:
    br              _end

# End of Assembly Source
    .end