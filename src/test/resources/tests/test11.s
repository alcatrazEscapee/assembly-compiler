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
    # This is true
    beq             r2, r0, _if1
    movi            r2, 2
    # This is also true
    ble             r2, r0, _if2
    movi            r2, 3
_if2:
    movi            r3, 1
_if1:
    movi            r4, 2
_end:
    br              _end

# End of Assembly Source
    .end