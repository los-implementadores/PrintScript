package org.printscript.common.ast;

import org.printscript.common.Position;

import java.util.Collections;
import java.util.List;

/**
 * Llamada a función.
 *
 * En PrintScript 1.0 la única función built-in es {@code println(expr)},
 * pero el nodo es genérico para soportar más adelante funciones de usuario.
 */
public final class CallExpression implements Expression {

    private final String callee;
    private final List<Expression> arguments;
    private final Position position;

    public CallExpression(String callee, List<Expression> arguments, Position position) {
        this.callee = callee;
        this.arguments = Collections.unmodifiableList(arguments);
        this.position = position;
    }

    /** Nombre de la función llamada. */
    public String getCallee() {
        return callee;
    }

    /** Lista inmutable de argumentos. */
    public List<Expression> getArguments() {
        return arguments;
    }

    @Override
    public Position getPosition() {
        return position;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitCallExpression(this);
    }
}
