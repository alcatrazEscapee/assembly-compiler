/*
 * Part of AssemblyCompiler
 * Copyright (c) 2019 - 2019 Alex O'Neill
 * See the project LICENCE.md for more information
 */

package compiler.keyword;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import compiler.component.ComponentStatic;
import compiler.component.Components;
import compiler.component.IComponent;
import compiler.component.IComponentManager;
import compiler.util.Helpers;
import compiler.util.conditionals.ConditionalExpressions;
import compiler.util.conditionals.IConditional;

import static compiler.component.IComponent.Flag.*;

public class KeywordWhile extends AbstractKeyword
{
    private Map<String, Integer> counter = new HashMap<>();

    @Override
    public boolean matches(String keyword, StringBuilder inputBuilder)
    {
        return IKeyword.matchKeyword(keyword, inputBuilder, "while");
    }

    @Override
    public void apply(String keyword, StringBuilder inputBuilder, IComponentManager compiler)
    {
        StringBuilder source = Helpers.nextLine(inputBuilder, ':', false);
        IComponent parent = compiler.getComponent(IComponent.Type.CURRENT);
        Stack<IComponent> controlStack = compiler.getControlStack();

        if (source.toString().equals("true"))
        {
            // get the counter for this function
            String functionName = parent.getFlag(FUNCTION_PREFIX);
            int value = counter.getOrDefault(functionName, 1);

            String label = "_while" + value;
            String result = IComponent.format("br", label) + "\n";
            parent.add(new ComponentStatic(label + ":\n").setFlag(LABEL, label).setFlag(TYPE, "label"));
            controlStack.add(new ComponentStatic(result).setFlag(LABEL, label).setFlag(TYPE, "break"));

            // Increment the counter in the map
            counter.put(functionName, value + 1);
            return;
        }

        // This is almost identical to the if statement logic, except the component placement is reversed (label first, break after)
        // get the counter for this function
        String functionName = parent.getFlag(FUNCTION_PREFIX);
        int value = counter.getOrDefault(functionName, 1);

        String label = functionName + "_while" + value;
        IConditional condition = ConditionalExpressions.create(label, source);
        parent.add(Components.label(label + "_a_t"));
        controlStack.add(condition);

        // Increment the counter in the map
        counter.put(functionName, value + 1);
    }

    @Override
    public void reset()
    {
        this.counter.clear();
    }
}
