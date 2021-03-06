/*
 * Part of AssemblyCompiler
 * Copyright (c) 2019 - 2019 Alex O'Neill
 * See the project LICENCE.md for more information
 */

package compiler.keyword;

import java.util.Set;

import compiler.component.ComponentStatic;
import compiler.component.ComponentVariable;
import compiler.component.IComponent;
import compiler.component.IComponentManager;
import compiler.util.InvalidAssemblyException;
import compiler.util.literal.IntResult;
import compiler.util.pattern.Patterns;

/**
 * All variable declarations must match one of hte following forms
 *
 * const name = VALUE       ->      .equ name, VALUE
 * int/byte name [= VALUE]  ->      name: .skip 4/1 / .word VALUE
 * int/byte[SIZE] name      ->      name: .skip 4*SIZE/SIZE
 * string name = "VALUE"    ->      name: .asciz "VALUE"
 * var[SIZE] name           ->      name: .skip SIZE
 */
public class KeywordVariable implements IKeyword
{
    private static final Set<String> VARIABLE_KEYWORDS = Set.of("var", "int", "byte", "string", "const");

    @Override
    public boolean matches(String keyword, StringBuilder inputBuilder)
    {
        for (String var : VARIABLE_KEYWORDS)
        {
            if (IKeyword.matchKeyword(keyword, inputBuilder, var))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public void apply(String keyword, StringBuilder inputBuilder, IComponentManager compiler)
    {
        switch (keyword)
        {
            case "int":
            case "byte":
                applyWord(keyword.equals("byte"), Patterns.END_OF_LINE.andThen(Patterns.TRIM_SPACE_ALL).apply(inputBuilder).get(), compiler);
                break;
            case "string":
                applyString(Patterns.END_OF_LINE.andThen(Patterns.TRIM_SPACES_DOUBLE_QUOTE).andThen(Patterns.IGNORE_DOUBLE_QUOTE).apply(inputBuilder).get(), compiler);
                break;
            case "var":
                applyVariable(Patterns.END_OF_LINE.andThen(Patterns.TRIM_SPACE_ALL).apply(inputBuilder).get(), compiler);
                break;
            case "const":
                applyConstant(Patterns.END_OF_LINE.andThen(Patterns.TRIM_SPACE_ALL).apply(inputBuilder).get(), compiler);

        }
    }

    private void applyWord(boolean isByte, StringBuilder source, IComponentManager compiler)
    {
        int size = isByte ? 1 : 4;
        // Variables with defined sizes
        String varName = Patterns.END_DELIMITER.apply(source).getString();
        if (source.length() == 0)
        {
            if (varName.equals(""))
            {
                throw new InvalidAssemblyException("error.message.blank_variable_name");
            }

            // Declaration with no assignment
            compiler.addComponent(new ComponentVariable(varName + ":\n" + IComponent.format(".skip", size + "\n"), !isByte));
        }
        else if (source.charAt(0) == '[')
        {
            // Remove the first '['
            source.deleteCharAt(0);
            String arraySize = Patterns.END_R_BRACKET.apply(source).getString();
            if (source.length() == 0 || source.charAt(0) != ']')
            {
                throw new InvalidAssemblyException("error.message.expected_keyword", "]");
            }
            // Remove the last ']'
            source.deleteCharAt(0);

            IntResult cast = new IntResult(arraySize, compiler);
            if (!cast.validLiteral())
            {
                throw new InvalidAssemblyException("error.message.invalid_literal", arraySize);
            }

            varName = Patterns.END_DELIMITER.apply(source).getString();
            if (varName.equals(""))
            {
                throw new InvalidAssemblyException("error.message.blank_variable_name");
            }

            if (source.length() != 0)
            {
                throw new InvalidAssemblyException("error.message.extra_keyword", source);
            }

            compiler.addComponent(new ComponentVariable(varName + ":\n" + IComponent.format(".skip", (size * cast.getValue()) + "\n"), !isByte));
        }
        else if (source.charAt(0) == '=')
        {
            source.deleteCharAt(0);
            String vars = source.toString().replace(",", ", ");
            if (isByte)
            {
                compiler.addComponent(new ComponentVariable(varName + ":\n" + IComponent.format(".byte", vars + "\n"), false));
            }
            else
            {
                compiler.addComponent(new ComponentVariable(varName + ":\n" + IComponent.format(".word", vars + "\n"), true));
            }
        }
    }

    private void applyString(StringBuilder source, IComponentManager compiler)
    {
        // Case: string name = "VALUE"
        String varName = Patterns.END_DELIMITER.apply(source).getString();
        if (source.length() == 0)
        {
            throw new InvalidAssemblyException("error.message.missing_assignment");
        }
        if (source.charAt(0) != '=')
        {
            throw new InvalidAssemblyException("error.message.expected_keyword", source, "=");
        }

        source.deleteCharAt(0);
        if (source.length() == 0 || source.charAt(0) != '\"')
        {
            throw new InvalidAssemblyException("error.message.expected_keyword", source, "\"");
        }

        String varValue = Patterns.END_OF_LINE.andThen(Patterns.IGNORE_DOUBLE_QUOTE).andThen(Patterns.TRIM_DOUBLE_QUOTE).apply(source).getString();
        compiler.addComponent(new ComponentVariable(varName + ":\n" +
                IComponent.format(".asciz", "\"" + varValue + "\"\n"), false));
    }

    private void applyVariable(StringBuilder source, IComponentManager compiler)
    {
        // Case: var[SIZE] name
        if (source.charAt(0) != '[')
        {
            throw new InvalidAssemblyException("error.message.expected_keyword", source, "[");
        }
        // Remove open bracket
        source.deleteCharAt(0);
        String varSize = Patterns.END_DELIMITER.apply(source).getString();

        if (source.length() == 0 || source.charAt(0) != ']')
        {
            throw new InvalidAssemblyException("error.message.expected_keyword", source, "]");
        }

        // Remove close bracket
        source.deleteCharAt(0);

        String rhs = Patterns.END_DELIMITER.apply(source).getString();
        if (source.length() != 0)
        {
            throw new InvalidAssemblyException("error.message.extra_keyword", source);
        }

        compiler.addComponent(new ComponentVariable(rhs + ":\n" +
                IComponent.format(".skip", varSize + "\n"), false));
    }

    private void applyConstant(StringBuilder source, IComponentManager compiler)
    {
        // Case: const name = VALUE
        String varName = Patterns.END_DELIMITER.apply(source).getString();
        if (source.length() == 0 || source.charAt(0) != '=')
        {
            throw new InvalidAssemblyException("error.message.missing_assignment");
        }
        // Remove '='
        source.deleteCharAt(0);

        IComponent cmp = compiler.getComponent(IComponent.Type.COMPILE);
        if (cmp == null)
        {
            throw new InvalidAssemblyException("error.message.missing_compile");
        }
        String result = IComponent.format(".equ", String.format("%s, %s\n", varName, source));

        compiler.addConstant(varName, source.toString());
        cmp.add(new ComponentStatic(result));
    }
}
