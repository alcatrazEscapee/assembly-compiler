package compiler.keyword;

import compiler.component.ComponentStatic;
import compiler.component.IComponent;
import compiler.component.IComponentManager;

public class KeywordComment implements IKeyword
{
    @Override
    public boolean matches(String keyword, StringBuilder inputBuilder)
    {
        return keyword.equals("//");
    }

    @Override
    public void apply(String keyword, StringBuilder inputBuilder, IComponentManager compiler)
    {
        IComponent parent = compiler.getComponent("current");
        if (parent != null)
        {
            parent.add(new ComponentStatic("\t#" + inputBuilder + "\n"));
            // Clear the rest of the line
            inputBuilder.delete(0, inputBuilder.length());
        }
    }
}
