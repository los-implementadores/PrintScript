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
 * {@code initializer} es {@code null} en una declaración sin valor ({@code let x: number;}).
 */
public final class VarDeclarationStatement implements Statement {

  private final Identifier name;
  private final String typeName; // el nombre del tipo, tal como se escribió
  private final Expression initializer;
  private final boolean isConst;
  private final Position position;

  public VarDeclarationStatement(
      Identifier name, String typeName, Expression initializer, Position position) {
    this(name, typeName, initializer, false, position);
  }

  public VarDeclarationStatement(
      Identifier name,
      String typeName,
      Expression initializer,
      boolean isConst,
      Position position) {
    this.name = name;
    this.typeName = typeName;
    this.initializer = initializer;
    this.isConst = isConst;
    this.position = position;
  }

  /**
   * {@code true} si la declaración es una constante ({@code const}), inmutable tras inicializar.
   */
  public boolean isConst() {
    return isConst;
  }

  public Identifier getName() {
    return name;
  }

  /** Nombre del tipo declarado (ej. {@code "number"}). */
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
