# Generated by Assembly Auto-Compiler by Alex O'Neill
# Setup
    .equ            LAST_RAM_WORD, 0x007FFFFC
    .global         _start
    .org            0x00000000
    .text

# Entry point
_start:
    movia           sp, LAST_RAM_WORD
_end:
    br              _end

# Random Variables

s:
    .asciz          "this is a string!"
others:
    .asciz          "POOP"

# End of Assembly Source
    .end