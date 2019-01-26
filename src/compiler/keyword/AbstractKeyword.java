package compiler.keyword;

public abstract class AbstractKeyword implements IKeyword
{
    static final String[] ALL = {"+", "-", "*", "/", "=", "&", "|", "^", "<<", ">>", "[", "]"};
    static final String[] OPERATORS = {"+", "-", "*", "/", "=", "&", "|", "^", "<<", ">>"};

    String getArg(StringBuilder source, String... delimiters)
    {
        StringBuilder arg = new StringBuilder();
        while (source.length() > 0 && validWordAhead(source, delimiters))
        {
            arg.append(source.charAt(0));
            source.deleteCharAt(0);
        }
        return arg.toString();
    }

    String getOp(StringBuilder source)
    {
        for (String op : OPERATORS)
        {
            if (source.length() >= op.length() && source.substring(0, op.length()).equals(op))
            {
                source.delete(0, op.length());
                return op;
            }
        }
        return "";
    }

    private boolean validWordAhead(StringBuilder source, String... delimiters)
    {
        for (String d : delimiters)
        {
            if (source.length() >= d.length() && d.equals(source.substring(0, d.length())))
            {
                return false;
            }
        }
        return true;
    }
}
