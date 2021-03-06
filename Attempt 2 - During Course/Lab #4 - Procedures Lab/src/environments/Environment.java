package environments;

import ast.Statement;
import javafx.util.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is the environment for the parsing process.
 * It will store all variables related to running the program.
 *
 * @author  Utkarsh Priyam
 * @version 3/27/20
 */
public class Environment
{
    private Environment parent;

    public Environment()
    {
        this.parent = null;
    }
    public Environment(Environment parent)
    {
        this.parent = parent;
    }

    // {Id, Value}
    private Map<String,Integer> variableMap = new HashMap<>();
    // {Id, [Arg Len, (Args, Exec Statement)]}
    private Map<String, Map<Integer, Pair<List<String>, Statement>>> procedureMap = new HashMap<>();

    public void declareVariable(String varName, int value)
    {
        variableMap.put(varName,value);
    }

    /**
     * Set the value of a variable with the given name
     * @param varName   Variable name
     * @param value     Value of variable
     */
    public void setVariable(String varName, int value)
    {
        if (variableMap.get(varName) != null)   // If variable exists in this scope
            variableMap.put(varName, value);
        else if (parent != null)                // Try parent scope recursively
            parent.setVariable(varName, value);
        else                                    // This is the root environment
            variableMap.put(varName, value);
    }

    /**
     * Get the value of variable with given name (default value of int = 0)
     * @param varName   Variable name
     * @return          Value of the variable, or default value if var doesn't exist
     */
    public int getVariable(String varName)
    {
        Integer integer = variableMap.get(varName);
        if (integer == null)
        {
            if (parent != null)
                integer = parent.getVariable(varName);
            else
                integer = 0;
        }
        return integer;
    }

    public void setProcedure(String procName, List<String> parameters, Statement exec)
    {
        Environment global = this;
        while (global.parent != null)
            global = global.parent;

        global.procedureMap.putIfAbsent(procName, new HashMap<>());
        global.procedureMap.get(procName).put(parameters.size(), new Pair<>(parameters,exec));
    }

    public Pair<List<String>, Statement> getProcedure(String procName, int argumentsLength)
    {
        Environment global = this;
        while (global.parent != null)
            global = global.parent;

        return global.procedureMap.get(procName).get(argumentsLength);
    }
}
