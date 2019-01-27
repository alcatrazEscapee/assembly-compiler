/*
 * Part of AssemblyCompiler by Alex O'Neill
 * Copyright (c) 2019 - 2019 Alex O'Neill
 * See the project LICENCE.md for more information
 */

package compiler.keyword;

import compiler.util.IComponentManagerStub;
import compiler.util.InvalidAssemblyException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KeywordRegisterExpressionTest
{
    private IKeyword keyword;
    private IComponentManagerStub stub;

    @BeforeEach
    void setUp()
    {
        keyword = new KeywordRegisterExpression();
        stub = new IComponentManagerStub();
    }

    @Test
    void matches()
    {
        assertTrue(keyword.matches("r0", new StringBuilder()));
        assertTrue(keyword.matches("sp", new StringBuilder()));
        assertTrue(keyword.matches("sstatus", new StringBuilder(" stuff")));

        assertFalse(keyword.matches("r0", new StringBuilder("x")));
        assertFalse(keyword.matches("r1", new StringBuilder("3")));
        assertFalse(keyword.matches("sp", new StringBuilder("s")));
    }

    @Test
    void apply1()
    {
        // rX = rY
        keyword.apply("r1", new StringBuilder(" = r2;"), stub);
        assertEquals("    mov             r1, r2\n", stub.compile());
    }

    @Test
    void apply2()
    {
        // rX = IMM
        keyword.apply("r2", new StringBuilder(" = 0xFF;"), stub);
        assertEquals("    movi            r2, 0xFF\n", stub.compile());
    }

    @Test
    void apply3()
    {
        // rX = IMM
        stub.addConstant("test", "1234");
        keyword.apply("r2", new StringBuilder(" = test;"), stub);
        assertEquals("    movi            r2, test\n", stub.compile());
    }

    @Test
    void apply4()
    {
        // rX = &VAR
        keyword.apply("r3", new StringBuilder(" = &test;"), stub);
        assertEquals("    movia           r3, test\n", stub.compile());
    }

    @Test
    void apply5()
    {
        // rX = (cast) VAR
        keyword.apply("r4", new StringBuilder(" = (byte) test;"), stub);
        assertEquals("    ldb             r4, test(r0)\n", stub.compile());
    }

    @Test
    void apply6()
    {
        // rX = (cast) &rY
        keyword.apply("r5", new StringBuilder(" = (io) &r6;"), stub);
        assertEquals("    ldwio           r5, 0(r6)\n", stub.compile());
    }

    @Test
    void apply7()
    {
        // rX = (cast) &rY[OFF]
        keyword.apply("r4", new StringBuilder(" = (byteio) &r5[4];"), stub);
        assertEquals("    ldbio           r4, 4(r5)\n", stub.compile());
    }

    @Test
    void apply8()
    {
        // rX = (cast) &rY[OFF]
        keyword.apply("r4", new StringBuilder(" = &r6[test];"), stub);
        assertEquals("    ldw             r4, test(r6)\n", stub.compile());
    }

    @Test
    void apply9()
    {
        // rX = rY OP rZ
        keyword.apply("r5", new StringBuilder(" = r6 + r7;"), stub);
        assertEquals("    add             r5, r6, r7\n", stub.compile());
    }

    @Test
    void apply10()
    {
        // rX = rY OP IMM
        keyword.apply("r5", new StringBuilder(" = sp & 4;"), stub);
        assertEquals("    andi            r5, sp, 4\n", stub.compile());
    }

    @Test
    void apply11()
    {
        // rX OP= rY
        keyword.apply("r5", new StringBuilder(" /= r0;"), stub);
        assertEquals("    div             r5, r5, r0\n", stub.compile());
    }

    @Test
    void apply12()
    {
        // rX OP= IMM
        keyword.apply("r5", new StringBuilder(" ?|= 0xFF;"), stub);
        assertEquals("    orhi            r5, r5, 0xFF\n", stub.compile());
    }

    @Test
    void applyException1()
    {
        // Unknown operator
        assertThrows(InvalidAssemblyException.class, () -> keyword.apply("r1", new StringBuilder(" < r2;"), stub));
    }

    @Test
    void applyException2()
    {
        // No immediate division
        assertThrows(InvalidAssemblyException.class, () -> keyword.apply("r1", new StringBuilder(" /= 3;"), stub));
    }

    @Test
    void applyException3()
    {
        // No register bitwise high operators
        assertThrows(InvalidAssemblyException.class, () -> keyword.apply("r1", new StringBuilder(" ?&= r2;"), stub));
    }

}