/*
 * Part of AssemblyCompiler
 * Copyright (c) 2019 - 2019 Alex O'Neill
 * See the project LICENCE.md for more information
 */

package compiler.keyword;

import compiler.component.ComponentStatic;
import compiler.component.Components;
import compiler.component.IComponent;
import compiler.component.IComponentManager;
import compiler.util.Helpers;
import compiler.util.InvalidAssemblyException;
import compiler.util.literal.CastResult;
import compiler.util.literal.IntResult;
import compiler.util.pattern.Patterns;

import static compiler.component.IComponent.Flag.WRITE_REGISTER;

/**
 * This class is responsible for all manner of register expressions
 * Each expression must be one of the following forms:
 *
 * rX = rY              ->      mov rX, rY
 * rX = IMM             ->      movi rX, IMM
 * rX = &VAR            ->      movia rX, VAR
 * rX = (literal) VAR      ->      ld(w/b)(io/) rX, VAR(r0)
 * rX = (literal) &rY      ->      ld(w/b)(io/) rX, 0(rY)
 * rX = (literal) &rY[OFF] ->      ld(w/b)(io/) rX, OFF(rY)
 * rX = rY OP rZ        ->      OP rX, rY, rZ
 * rX = rY OP IMM       ->      OPi rX, rY, IMM
 * rX OP= rY            ->      OP rX, rX, rY
 * rX OP= IMM           ->      OPi rX, rX, IMM
 * rX UOP               ->      OPi rX, rX, 1 (for ++ / --)
 *
 * Note that * can be interchanged with & for no particular reason
 *
 * See {@link Components}
 */
public class KeywordRegisterExpression implements IKeyword
{
    @Override
    public boolean matches(String keyword, StringBuilder inputBuilder)
    {
        for (String reg : Helpers.REGISTERS)
        {
            if (IKeyword.matchKeyword(keyword, inputBuilder, reg))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public void apply(String keyword, StringBuilder inputBuilder, IComponentManager compiler)
    {
        IComponent parent = compiler.getComponent(IComponent.Type.CURRENT);
        StringBuilder source = Patterns.END_OF_LINE.andThen(Patterns.IGNORE_SINGLE_QUOTE).andThen(Patterns.TRIM_SPACES_SINGLE_QUOTE).apply(inputBuilder).get();

        if (parent == null)
        {
            throw new InvalidAssemblyException("error.message.extra_keyword", keyword);
        }

        if (source.charAt(0) == '=')
        {
            source.deleteCharAt(0);
            String lhs = Patterns.END_DELIMITER.andThen(Patterns.IGNORE_FIRST_MINUS).andThen(Patterns.IGNORE_SINGLE_QUOTE).apply(source).getString();

            if (Helpers.REGISTERS.contains(lhs))
            {
                // Cases: rX = rY OP rz / rX = rY OP IMM / rX = rY
                String op = Patterns.NEXT_OPERATOR.apply(source).getString();
                String rhs = Patterns.END_DELIMITER.andThen(Patterns.IGNORE_FIRST_MINUS).apply(source).getString();

                if (op.equals(""))
                {
                    // Case: rX = rY
                    String result = IComponent.format("mov", keyword + ", " + lhs + "\n");
                    parent.add(new ComponentStatic(result).setFlag(WRITE_REGISTER, keyword));
                }
                else if (Helpers.REGISTERS.contains(rhs))
                {
                    // Case: rX = rY OP rZ
                    parent.add(Components.op(keyword, lhs, op, rhs));
                }
                else
                {
                    // Case: rX = rY OP IMM
                    parent.add(Components.opi(keyword, lhs, op, rhs));
                }
            }
            else
            {
                // Casting flags
                CastResult cast = new CastResult(lhs);
                lhs = cast.getResult();

                // Case: rX = IMM / rX = (literal) &VAR / rX = (literal) &rY
                if (lhs.length() == 0 && (source.charAt(0) == '&' || source.charAt(0) == '*'))
                {
                    // Remove the '&' or '*'
                    source.deleteCharAt(0);
                    String rhs = Patterns.END_DELIMITER.apply(source).getString();
                    if (Helpers.REGISTERS.contains(rhs))
                    {
                        String offset = "0";

                        if (source.length() > 0 && source.charAt(0) == '[')
                        {
                            // Remove leading '['
                            source.deleteCharAt(0);
                            offset = Patterns.END_R_BRACKET.apply(source).getString();
                            // Remove ending ']'
                            source.deleteCharAt(0);
                        }

                        // Case rX = (literal) &rY / rX = (literal) &rY[OFF]
                        String cmd = cast.makeLoad();
                        String result = IComponent.format(cmd, String.format("%s, %s(%s)\n", keyword, offset, rhs));
                        parent.add(new ComponentStatic(result).setFlag(WRITE_REGISTER, keyword));
                    }
                    else
                    {
                        // Case: rX = &VAR
                        String result = IComponent.format("movia", keyword + ", " + rhs + "\n");
                        parent.add(new ComponentStatic(result).setFlag(WRITE_REGISTER, keyword));
                    }
                }
                else
                {
                    IntResult intResult = new IntResult(lhs, compiler);
                    if (intResult.validLiteral())
                    {
                        // Case rX = IMM
                        String result = IComponent.format("movi", keyword + ", " + lhs + "\n");
                        parent.add(new ComponentStatic(result).setFlag(WRITE_REGISTER, keyword));
                    }
                    else
                    {
                        // Case: rX = (literal) VAR
                        String cmd = cast.makeLoad();
                        if (!Helpers.isValidName(lhs))
                        {
                            throw new InvalidAssemblyException("error.message.invalid_variable_name", lhs);
                        }
                        String result = IComponent.format(cmd, keyword + ", " + lhs + "(r0)\n");
                        parent.add(new ComponentStatic(result).setFlag(WRITE_REGISTER, keyword));
                    }
                }
            }
        }
        else if (source.toString().equals("++") || source.toString().equals("--"))
        {
            // Case: rX UOP
            parent.add(Components.opi(keyword, keyword, String.valueOf(source.charAt(0)), "1"));
            source.delete(0, 2);
        }
        else
        {
            // Cases: rX OP= imm / rX OP= rY
            String op = Patterns.NEXT_OPERATOR.apply(source).getString();
            if (source.charAt(0) != '=')
            {
                throw new InvalidAssemblyException("error.message.unknown_assignment_operator", source);
            }
            // Remove the '='
            source.deleteCharAt(0);

            String rhs = Patterns.END_DELIMITER.andThen(Patterns.IGNORE_FIRST_MINUS).apply(source).getString();
            if (Helpers.REGISTERS.contains(rhs))
            {
                // Case: rX OP= rY
                parent.add(Components.op(keyword, keyword, op, rhs));
            }
            else
            {
                // Case: rX OP= IMM
                parent.add(Components.opi(keyword, keyword, op, rhs));
            }
        }

        //System.out.println("Reached the end with + " + source);
        if (source.length() > 0) throw new InvalidAssemblyException("HAHAHAHAH");
    }
}
