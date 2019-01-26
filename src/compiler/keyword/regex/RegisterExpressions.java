package compiler.keyword.regex;

import java.util.HashMap;
import java.util.Map;

import compiler.component.IComponent;
import compiler.util.InvalidAssemblyException;

public class RegisterExpressions
{
    private static final Map<String, String> EXPRESSIONS = new HashMap<>();
    private static final Map<String, String> COMPARISONS = new HashMap<>();
    private static final Map<String, String> COMPARISONS_INVERTED = new HashMap<>();

    static
    {
        EXPRESSIONS.put("+", "add");
        EXPRESSIONS.put("-", "sub");
        EXPRESSIONS.put("*", "mul");
        EXPRESSIONS.put("/", "div");
        EXPRESSIONS.put("|", "or");
        EXPRESSIONS.put("&", "and");

        COMPARISONS.put("<=", "ble");
        COMPARISONS.put(">=", "bge");
        COMPARISONS.put("==", "beq");
        COMPARISONS.put("!=", "bne");
        COMPARISONS.put("<", "blt");
        COMPARISONS.put(">", "bgt");

        COMPARISONS_INVERTED.put("<=", "bgt");
        COMPARISONS_INVERTED.put(">=", "blt");
        COMPARISONS_INVERTED.put("==", "bne");
        COMPARISONS_INVERTED.put("!=", "beq");
        COMPARISONS_INVERTED.put("<", "bge");
        COMPARISONS_INVERTED.put(">", "ble");
    }

    public static String of(String rX, String rY, String op, String rZ)
    {
        if (!EXPRESSIONS.containsKey(op))
        {
            throw new InvalidAssemblyException("Unknown operation " + op);
        }
        return IComponent.format(EXPRESSIONS.get(op), String.format("%s, %s, %s\n", rX, rY, rZ));
    }

    public static String ofImm(String rX, String rY, String op, String imm)
    {
        if (op.equals("/"))
        {
            throw new InvalidAssemblyException("Division cannot be done with immeadiate values");
        }
        return IComponent.format(EXPRESSIONS.get(op) + "i", String.format("%s, %s, %s\n", rX, rY, imm));
    }

    public static String ofComp(String rX, String rY, String op, String label)
    {
        if (!COMPARISONS.containsKey(op))
        {
            throw new InvalidAssemblyException("Unknown comparison " + op);
        }
        return IComponent.format(COMPARISONS.get(op), String.format("%s, %s, %s\n", rX, rY, label));
    }

    public static String ofCompInverted(String rX, String rY, String op, String label)
    {
        if (!COMPARISONS_INVERTED.containsKey(op))
        {
            throw new InvalidAssemblyException("Unknown comparison " + op);
        }
        return IComponent.format(COMPARISONS_INVERTED.get(op), String.format("%s, %s, %s\n", rX, rY, label));
    }
}