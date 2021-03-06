package me.utkarshpriyam.CodeGenerationLab.Parsers;

import me.utkarshpriyam.CodeGenerationLab.AST.*;
import me.utkarshpriyam.CodeGenerationLab.AST.Number;
import me.utkarshpriyam.CodeGenerationLab.Scanner;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Parser
{
    private Scanner scanner;
    private String currentToken;

    public Parser(Scanner scanner)
    {
        this.scanner = scanner;
        currentToken = scanner.nextToken();
    }

    private void eat(String currentTokenCheck)
    {
        if (currentToken.equals(currentTokenCheck))
            currentToken = scanner.nextToken();
        else
            throw new IllegalArgumentException("Expected current token \'" + currentToken +
                    "\' but found \'" + currentTokenCheck + "\'");
    }

    private Expression parseInteger()
    {
        Number integer = new Number(Integer.parseInt(currentToken));
        eat(currentToken);
        return integer;
    }

    public Program parseProgram()
    {
        List<String> variables = new LinkedList<>();
        while (currentToken.equals("VAR"))
        {
            eat("VAR");
            variables.add(currentToken);
            eat(currentToken);
            while (currentToken.equals(","))
            {
                eat(",");
                variables.add(currentToken);
                eat(currentToken);
            }
            eat(";");
        }

        List<Statement> procedureDeclarations = new LinkedList<>();
        while (currentToken.equals("PROCEDURE"))
            procedureDeclarations.add(parseProcedureDeclaration());

        Program program = new Program(variables, procedureDeclarations, parseStatement());
        eat(Scanner.END_OF_FILE_TOKEN);
        return program;
    }

    private Statement parseProcedureDeclaration()
    {
        eat("PROCEDURE");
        String procName = currentToken;
        eat(currentToken);
        List<String> parameters = parseParameters();
        eat(";");
        Statement procDo = parseStatement();
        return new ProcedureDeclaration(procName, parameters, procDo);
    }

    private List<String> parseParameters()
    {
        List<String> parameters = new LinkedList<>();
        eat("(");
        if (currentToken.equals(")"))
        {
            eat(currentToken);
            return parameters;
        }
        parameters.add(currentToken);
        eat(currentToken);
        while (currentToken.equals(","))
        {
            eat(",");
            parameters.add(currentToken);
            eat(currentToken);
        }
        eat(")");
        return parameters;
    }

    public Statement parseStatement()
    {
        if (Scanner.END_OF_FILE_TOKEN.equals(currentToken))
            throw new RuntimeException("Done Parsing File");

        switch (currentToken)
        {
            case "WRITELN":
            {
                eat("WRITELN");
                eat("(");
                Expression exp = parseExpression();
                eat(")");
                eat(";");
                return new Writeln(exp);
            }
            case "BEGIN":
                List<Statement> statements = new LinkedList<>();
                eat("BEGIN");
                while (!currentToken.equals("END"))
                    statements.add(parseStatement());
                eat("END");
                eat(";");
                return new Block(statements);
            case "IF":
            {
                eat("IF");
                Condition cond = parseCondition();
                eat("THEN");
                Statement then = parseStatement();
                if (currentToken.equals("ELSE"))
                {
                    eat("ELSE");
                    Statement or = parseStatement();
                    return new If(cond, then, or);
                } else
                    return new If(cond, then);
            }
            case "WHILE":
            {
                eat("WHILE");
                Condition cond = parseCondition();
                eat("DO");
                Statement then = parseStatement();
                return new While(cond, then);
            }
            case "FOR":
            {
                eat("FOR");
                String varName = currentToken;
                eat(currentToken);
                eat(":=");
                Expression start = parseExpression();
                eat("TO");
                Expression end = parseExpression();
                eat("DO");

                // count = start
                Statement pre = new Assignment(varName, start);
                // count <= end
                Condition cond = new Condition("<=", new Variable(varName), end);
                // count++
                Statement change = new Assignment(varName, new BinOp("+", new Variable(varName), new Number(1)));

                // For loop
                Statement then = parseStatement();
                return new For(pre, cond, change, then);
            }
            case "EXIT":
            {
                eat("EXIT");
                eat(";");
                return new Exit();
            }
            case "BREAK":
            {
                eat("BREAK");
                eat(";");
                return new Break();
            }
            case "CONTINUE":
            {
                eat("CONTINUE");
                eat(";");
                return new Continue();
            }
            default:
            {
                String varName = currentToken;
                eat(currentToken);
                if (currentToken.equals(":="))
                {
                    eat(":=");
                    Expression exp = parseExpression();
                    eat(";");
                    return new Assignment(varName, exp);
                }
                else // currentToken.equals("(") -- Procedure call
                {
                    List<Expression> arguments = parseArguments();
                    eat(";");
                    return new VoidProcedureCall(varName, arguments);
                }
            }
        }
    }

    private List<Expression> parseArguments()
    {
        List<Expression> arguments = new LinkedList<>();
        eat("(");
        if (currentToken.equals(")"))
        {
            eat(currentToken);
            return arguments;
        }
        arguments.add(parseExpression());
        while (currentToken.equals(","))
        {
            eat(",");
            arguments.add(parseExpression());
        }
        eat(")");
        return arguments;
    }

    private Condition parseCondition()
    {
        Expression exp1, exp2;
        exp1 = parseExpression();
        String relOp = currentToken;
        eat(currentToken);
        exp2 = parseExpression();
        return new Condition(relOp, exp1, exp2);
    }

    private Expression parseFactor()
    {
        if (currentToken.equals("-"))
        {
            eat(currentToken);
            return new BinOp("-", new Number(0), parseFactor());
        }
        if (currentToken.equals("("))
        {
            eat("(");
            Expression exp = parseExpression();
            eat(")");
            return exp;
        }
        if(!isNumber(currentToken))
        {
            String varName = currentToken;
            eat(currentToken);
            if (currentToken.equals("("))
            {
                List<Expression> arguments = parseArguments();
                return new ProcedureCall(varName, arguments);
            }
            return new Variable(varName);
        }
        return parseInteger();
    }

    private boolean isNumber(String currentToken)
    {
        char[] charArr = currentToken.toCharArray();
        for (char c: charArr)
            if (!('0' <= c && c <= '9'))
                return false;
        return true;
    }

    private Expression parseTerm()
    {
        Expression val = parseFactor();
        while (currentToken.equals("*") || currentToken.equals("/"))
        {
            String oldToken = currentToken;
            eat(currentToken);
            val = new BinOp(oldToken, val, parseFactor());
        }
        return val;
    }

    private Expression parseExpression()
    {
        Expression val = parseTerm();
        while (currentToken.equals("+") || currentToken.equals("-"))
        {
            String oldToken = currentToken;
            eat(currentToken);
            val = new BinOp(oldToken, val, parseTerm());
        }
        return val;
    }
}
