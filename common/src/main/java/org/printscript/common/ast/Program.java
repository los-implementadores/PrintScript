package org.printscript.common.ast;

import org.printscript.common.Position;

import java.util.Collections;
import java.util.List;

/**
 * Nodo raíz del AST: representa el programa completo como una lista de sentencias.
 */
public final class Program implements Node {

    private final List<Statement> statements;
    private final Position position;

    public Program(List<Statement> statements, Position position) {
        this.statements = Collections.unmodifiableList(statements);
        this.position = position;
    }

    /** Lista inmutable de todas las sentencias del programa, en orden. */
    public List<Statement> getStatements() {
        return statements;
    }

    @Override
    public Position getPosition() {
        return position;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitProgram(this);
    }
}
