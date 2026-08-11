package org.printscript.common.ast;

import org.printscript.common.Position;

/**
 * Referencia a un identificador (nombre de variable o función).
 * P.ej. {@code name} en {@code println(name)}.
 */
public final class Identifier implements Expression {

    private final String name;
    private final Position position;

    public Identifier(String name, Position position) {
        this.name = name;
        this.position = position;
    }

    public String getName() {
        return name;
    }

    @Override
    public Position getPosition() {
        return position;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitIdentifier(this);
    }
}
