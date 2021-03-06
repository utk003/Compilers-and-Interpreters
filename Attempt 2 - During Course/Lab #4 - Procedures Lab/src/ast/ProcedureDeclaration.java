package ast;

import environments.Environment;

import java.util.List;

/**
 * This class handles declaring procedures
 * When executed, it declares the procedure
 *
 * @author  Utkarsh Priyam
 * @version 4/7/20
 */
public class ProcedureDeclaration extends Statement
{
    private String id;
    private List<String> parameters;
    private Statement statement;

    /**
     * Create a new ProcedureDeclaration with the given parameters
     * @param id            Procedure name
     * @param parameters    Parameter list
     * @param statement     Procedure body
     */
    public ProcedureDeclaration(String id, List<String> parameters, Statement statement)
    {
        this.id = id;
        this.parameters = parameters;
        this.statement = statement;
    }

    /**
     * Declare the procedure in the given environment
     * @param env   The execution environment
     */
    @Override
    public void exec(Environment env)
    {
        env.setProcedure(id, parameters, statement);
    }
}
