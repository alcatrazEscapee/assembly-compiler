# Generated by Assembly Auto-Compiler by Alex O'Neill
# Setup
    .equ            LAST_RAM_WORD, 0x007FFFFC
    .global         _start
    .org            0x00000000
    .text

# Entry point
_start:
    movia           sp, LAST_RAM_WORD
    movi            r2, 0
    call            EarlyReturn
    addi            r2, r2, 1
    call            EarlyReturn
_end:
    br              _end

# ========== EarlyReturn ==========
EarlyReturn:
    ble             r2, r0, er_if1
    br              er_ret
er_if1:
    subi            r2, r2, 1
er_ret:
    ret

# End of Assembly Source
    .end