/*
 * Part of AssemblyCompiler
 * Copyright (c) 2019 - 2019 Alex O'Neill
 * See the project LICENCE.md for more information
 */

package compiler.keyword;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import compiler.component.ComponentLabel;
import compiler.component.Components;
import compiler.component.IComponent;
import compiler.component.IComponentManager;
import compiler.util.Helpers;
import compiler.util.InvalidAssemblyException;
import compiler.util.RegisterExpressions;

import static compiler.component.IComponent.Flag.FUNCTION_PREFIX;
import static compiler.component.IComponent.Flag.TYPE;

public class KeywordIf extends AbstractKeyword
{
    private final Map<String, Integer> counter = new HashMap<>();

    @Override
    public boolean matches(String keyword, StringBuilder inputBuilder)
    {
        return IKeyword.matchKeyword(keyword, inputBuilder, "if");
    }

    @Override
    public void apply(String keyword, StringBuilder inputBuilder, IComponentManager compiler)
    {
        StringBuilder source = Helpers.nextLine(inputBuilder, ':', false);
        IComponent parent = compiler.getComponent(IComponent.Type.CURRENT);
        Stack<IComponent> controlStack = compiler.getControlStack();

        String lhs = getArg(source, COMPARATORS);
        if (!REGISTERS.contains(lhs))
        {
            throw new InvalidAssemblyException("Unable to do an if statement with LHS not a register: " + lhs + source);
        }

        String op = getOp(source, COMPARATORS);
        if (op.equals(""))
        {
            throw new InvalidAssemblyException("Unknown comparison operator " + source);
        }

        String rhs = getArg(source, ":");
        if (!REGISTERS.contains(rhs))
        {
            throw new InvalidAssemblyException("Unable to do an if statement with RHS not a register: " + rhs + source);
        }

        // get the counter for this function
        String functionName = parent.getFlag(FUNCTION_PREFIX);
        int value = counter.getOrDefault(functionName, 1);

        String label = functionName + "_if" + value;
        String result = RegisterExpressions.ofCompInverted(lhs, rhs, op, "%s");
        parent.add(new ComponentLabel(result, label).setFlag(TYPE, "break_conditional"));
        controlStack.add(Components.label(label));

        // Increment the counter in the map
        counter.put(functionName, value + 1);
    }

    @Override
    public void reset()
    {
        this.counter.clear();
    }
}
