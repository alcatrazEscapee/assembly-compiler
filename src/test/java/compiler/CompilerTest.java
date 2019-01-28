/*
 * Part of AssemblyCompiler by Alex O'Neill
 * Copyright (c) 2019 - 2019 Alex O'Neill
 * See the project LICENCE.md for more information
 */

package compiler;


import compiler.util.Helpers;
import compiler.util.InvalidAssemblyException;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class CompilerTest
{
    @TestFactory
    Stream<DynamicTest> testMatches()
    {
        return IntStream.rangeClosed(1, 22).mapToObj(x -> "test" + x).map(x -> DynamicTest.dynamicTest(x, () -> {
            String inputFile = loadFile("tests/" + x + ".sc");
            String outputFile = loadFile("tests/" + x + ".s");
            assertEquals(outputFile, AssemblyCompiler.INSTANCE.compile(inputFile));
        }));
    }

    @TestFactory
    Stream<DynamicTest> testExceptions()
    {
        return IntStream.rangeClosed(1, 4).mapToObj(x -> "exc" + x).map(x -> DynamicTest.dynamicTest(x, () -> {
            String inputFile = loadFile("tests/" + x + ".sc");
            InvalidAssemblyException t = assertThrows(InvalidAssemblyException.class, () -> AssemblyCompiler.INSTANCE.compile(inputFile));
        }));
    }

    private String loadFile(String fileName)
    {
        InputStream input = CompilerTest.class.getClassLoader().getResourceAsStream(fileName);
        if (input == null)
        {
            fail("Input stream is null");
            return "";
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input)))
        {
            return reader.lines().reduce((x, y) -> x + "\n" + y).orElse("");
        }
        catch (IOException e)
        {
            fail("Unable to load file for testing");
            return "";
        }
    }
}