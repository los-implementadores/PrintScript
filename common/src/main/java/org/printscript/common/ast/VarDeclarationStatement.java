package org.printscript.common.ast;

import org.printscript.common.Position;

/**
 * Declaración de variable con tipo y valor inicial.
 *
 * <pre>
 *   let name: string = "Joe";
 *   let age:  number = 30;
 * </pre>
 *
 * {@code initializer} puede ser {@code null} si en el futuro se permiten declaraciones sin
 * asignación, pero en PrintScript 1.0 siempre tiene valor.
 */
public final class VarDeclarationStatement implements Statement {

  private final Identifier name;
  private final String typeName; // "number" o "string"
  private final Expression initializer;
  private final Position position;

  public VarDeclarationStatement(
      Identifier name, String typeName, Expression initializer, Position position) {
    this.name = name;
    this.typeName = typeName;
    this.initializer = initializer;
    this.position = position;
  }

  public Identifier getName() {
    return name;
  }

  /** Nombre del tipo declarado: {@code "number"} o {@code "string"}. */
  public String getTypeName() {
    return typeName;
  }

  /** Expresión del lado derecho del {@code =}. Puede ser {@code null}. */
  public Expression getInitializer() {
    return initializer;
  }

  @Override
  public Position getPosition() {
    return position;
  }

  @Override
  public <T> T accept(ASTVisitor<T> visitor) {
    return visitor.visitVarDeclaration(this);
  }
}
