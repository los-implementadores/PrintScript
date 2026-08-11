package org.printscript.common.ast;

import org.printscript.common.Position;

/**
 * Operación binaria entre dos expresiones.
 *
 * El operador es el lexema del token ({@code +}, {@code -}, {@code *}, {@code /}).
 */
public final class BinaryExpression implements Expression {

    private final Expression left;
    private final String operator;
    private final Expression right;
    private final Position position;

    public BinaryExpression(Expression left, String operator, Expression right, Position position) {
        this.left = left;
        this.operator = operator;
        this.right = right;
        this.position = position;
    }

    public Expression getLeft() {
        return left;
    }

    public String getOperator() {
        return operator;
    }

    public Expression getRight() {
        return right;
    }

    @Override
    public Position getPosition() {
        return position;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitBinaryExpression(this);
    }
}
