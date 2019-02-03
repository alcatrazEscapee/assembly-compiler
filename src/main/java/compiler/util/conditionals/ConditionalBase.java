/*
 * Part of AssemblyCompiler
 * Copyright (c) 2019 - 2019 Alex O'Neill
 * See the project LICENCE.md for more information
 */

package compiler.util.conditionals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import compiler.component.ComponentLabel;
import compiler.component.Components;
import compiler.component.IComponent;
import compiler.util.Helpers;
import compiler.util.InvalidAssemblyException;
import compiler.util.RegisterExpressions;

import static compiler.component.IComponent.Flag.TYPE;
import static compiler.keyword.IKeyword.COMPARATORS;
import static compiler.keyword.IKeyword.REGISTERS;

public class ConditionalBase extends AbstractConditional
{
    private final String name;
    private final List<IComponent> components;

    ConditionalBase(String name, StringBuilder source)
    {
        this.name = name;

        String lhs = Helpers.getFromList(source, REGISTERS);
        if (!REGISTERS.contains(lhs))
        {
            throw new InvalidAssemblyException("Unable to do an if statement with LHS not a register: " + lhs + source);
        }

        String op = Helpers.getFromList(source, COMPARATORS);
        if (op.equals(""))
        {
            throw new InvalidAssemblyException("Unknown comparison operator " + source);
        }

        String rhs = source.toString();
        if (!REGISTERS.contains(rhs))
        {
            throw new InvalidAssemblyException("Unable to do an if statement with RHS not a register: " + rhs + source);
        }

        this.components = new ArrayList<>();
        Collections.addAll(components,
                new ComponentLabel(RegisterExpressions.ofComp(lhs, rhs, op, "%s"), name + "_t").setFlag(TYPE, "break_conditional"),
                Components.br(name + "_f")
        );
    }

    @Override
    public List<IComponent> build()
    {
        return components;
    }

    @Override
    public String getName()
    {
        return name;
    }
}
