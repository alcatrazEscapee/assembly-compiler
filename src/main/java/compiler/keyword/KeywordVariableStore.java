/*
 * Part of AssemblyCompiler
 * Copyright (c) 2019 - 2019 Alex O'Neill
 * See the project LICENCE.md for more information
 */

package compiler.keyword;

import compiler.component.ComponentStatic;
import compiler.component.IComponent;
import compiler.component.IComponentManager;
import compiler.util.Helpers;
import compiler.util.InvalidAssemblyException;

/**
 * This class is responsible for variable store commands.
 * This is a special subset of register expressions that don't start with rX.
 * Each command must match one of the following cases:
 *
 * VAR = (cast) rX      ->      st(w/b)(io/) rX, VAR(r0)
 * *rX = (cast) rY      ->      st(w/b)(io/) rY, 0(rX)
 * *rX[OFF] = (cast) rY ->      st(w/b)(io/) rY, OFF(rX)
 *
 * Note that * can be interchanged with & for no particular reason
 */
public class KeywordVariableStore implements IKeyword
{
    @Override
    public boolean matches(String keyword, StringBuilder inputBuilder)
    {
        return (keyword.endsWith("=") && !REGISTERS.contains(keyword.substring(0, keyword.length() - 1))) || keyword.equals("*") || keyword.equals("&");
    }

    @Override
    public void apply(String keyword, StringBuilder inputBuilder, IComponentManager compiler)
    {
        StringBuilder source = Helpers.nextLine(inputBuilder);
        IComponent parent = compiler.getComponent(IComponent.Type.CURRENT);
        if (parent == null)
        {
            throw new InvalidAssemblyException("Variable store found outside function");
        }
        boolean byteFlag = false, ioFlag = false;

        if (keyword.equals("*") || keyword.equals("&"))
        {
            String lhs = Helpers.matchUntil(source, '=', '[');
            if (!REGISTERS.contains(lhs))
            {
                throw new InvalidAssemblyException("Unable to do variable store with pointer to not a register " + lhs);
            }
            if (source.length() == 0)
            {
                throw new InvalidAssemblyException("Unable to do variable store without assignment " + lhs + source);
            }

            String offset = "0";
            if (source.charAt(0) == '[')
            {
                // Delete leading '['
                source.deleteCharAt(0);
                offset = Helpers.matchUntil(source, ']');
                // Delete ending ']'
                source.deleteCharAt(0);
            }

            // Delete '='
            source.deleteCharAt(0);
            String rhs = Helpers.matchUntil(source);

            // Casting flags
            if (rhs.length() >= 4 && rhs.startsWith("(io)"))
            {
                ioFlag = true;
                rhs = rhs.substring(4);
            }
            if (rhs.length() >= 6 && rhs.startsWith("(byte)"))
            {
                byteFlag = true;
                rhs = rhs.substring(6);
            }
            if (rhs.length() >= 8 && rhs.startsWith("(byteio)"))
            {
                byteFlag = ioFlag = true;
                rhs = rhs.substring(8);
            }
            if (!REGISTERS.contains(rhs))
            {
                throw new InvalidAssemblyException("Unable to do variable store from not a register " + rhs + " " + source);
            }
            String cmd = makeStore(byteFlag, ioFlag);
            String result = IComponent.format(cmd, String.format("%s, %s(%s)\n", rhs, offset, lhs));
            parent.add(new ComponentStatic(result));

        }
        else
        {
            String varName = keyword.substring(0, keyword.length() - 1).replace(" ", "");
            String rhs = Helpers.matchUntil(source, DELIMITERS);

            // Casting flags
            if (rhs.length() >= 4 && rhs.startsWith("(io)"))
            {
                ioFlag = true;
                rhs = rhs.substring(4);
            }
            if (rhs.length() >= 6 && rhs.startsWith("(byte)"))
            {
                byteFlag = true;
                rhs = rhs.substring(6);
            }
            if (rhs.length() >= 8 && rhs.startsWith("(byteio)"))
            {
                byteFlag = ioFlag = true;
                rhs = rhs.substring(8);
            }
            // variable = rX
            String cmd = makeStore(byteFlag, ioFlag);
            parent.add(new ComponentStatic(IComponent.format(cmd, rhs + ", " + varName + "(r0)\n")));
        }
    }

    private String makeStore(boolean byteFlag, boolean ioFlag)
    {
        return "st" + (byteFlag ? "b" : "w") + (ioFlag ? "io" : "");
    }
}
