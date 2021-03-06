package me.utkarshpriyam.PascalParserLab.AST;

import me.utkarshpriyam.PascalParserLab.Environments.Environment;

import java.util.List;

public class Block extends Statement
{
    private List<Statement> statements;

    public Block(List<Statement> statements)
    {
        this.statements = statements;
    }

    @Override
    public void exec(Environment env)
    {
        for (Statement statement: statements)
            statement.exec(env);
    }
}
