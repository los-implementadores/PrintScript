package org.printscript.interpreter;

import org.printscript.common.ast.*;
import org.printscript.common.env.Environment;

import java.util.Iterator;

public class InterpreterVisitor implements ASTVisitor<Object> {

    private final Environment env;

    public InterpreterVisitor(Environment env) {
        this.env = env;
    }

    @Override
    public Object visitProgram(Program node) {
        Iterator<Statement> it = node.stream();
        while (it.hasNext()) {
            it.next().accept(this);
        }
        return null;
    }

    @Override
    public Object visitVarDeclaration(VarDeclarationStatement node) {
        String name = node.getName().getName();
        String type = node.getTypeName();
        Object value = null;

        if (node.getInitializer() != null) {
            value = node.getInitializer().accept(this);
        }

        env.define(name, type, value);
        return null;
    }

    @Override
    public Object visitAssignment(AssignmentStatement node) {
        String name = node.getTarget().getName();
        Object value = node.getValue().accept(this);

        env.assign(name, value);
        return null;
    }

    @Override
    public Object visitExpressionStatement(ExpressionStatement node) {
        node.getExpression().accept(this);
        return null;
    }

    @Override
    public Object visitBinaryExpression(BinaryExpression node) {
        Object left = node.getLeft().accept(this);
        Object right = node.getRight().accept(this);
        String op = node.getOperator();

        if (op.equals("+")) {
            if (left instanceof String || right instanceof String) {
                return formatPrintValue(left) + formatPrintValue(right);
            }
            return ((Double) left) + ((Double) right);
        }

        double l = (Double) left;
        double r = (Double) right;

        return switch (op) {
            case "-" -> l - r;
            case "*" -> l * r;
            case "/" -> {
                if (r == 0) {
                    throw new RuntimeException("Execution Error at " + node.getPosition() + ": Division by zero.");
                }
                yield l / r;
            }
            default -> throw new RuntimeException("Unknown operator: " + op);
        };
    }

    @Override
    public Object visitCallExpression(CallExpression node) {
        if (node.getCallee().equals("println")) {
            Object val = node.getArguments().get(0).accept(this);
            System.out.println(formatPrintValue(val));
        }
        return null;
    }

    @Override
    public Object visitNumberLiteral(NumberLiteral node) {
        return node.getValue();
    }

    @Override
    public Object visitStringLiteral(StringLiteral node) {
        return node.getValue();
    }

    @Override
    public Object visitIdentifier(Identifier node) {
        return env.get(node.getName());
    }

    private String formatPrintValue(Object val) {
        if (val instanceof Double d) {
            if (d % 1 == 0) {
                return String.valueOf(d.longValue());
            }
        }
        return String.valueOf(val);
    }
}