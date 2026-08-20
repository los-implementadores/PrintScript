package org.printscript.common.ast;

import org.printscript.common.Position;

/**
 * Asignación a una variable ya declarada.
 *
 * <pre>
 *   a = a / b;
 *   name = "otro";
 * </pre>
 *
 * Distinto de {@link VarDeclarationStatement}, que declara e inicializa en una sola sentencia
 * ({@code let x: number = 5;}). Acá la variable ya existe y solo se actualiza su valor.
 */
public final class AssignmentStatement implements Statement {

  private final Identifier target;
  private final Expression value;
  private final Position position;

  public AssignmentStatement(Identifier target, Expression value, Position position) {
    this.target = target;
    this.value = value;
    this.position = position;
  }

  /** Identificador de la variable que recibe el nuevo valor. */
  public Identifier getTarget() {
    return target;
  }

  /** Expresión del lado derecho del {@code =}. */
  public Expression getValue() {
    return value;
  }

  @Override
  public Position getPosition() {
    return position;
  }

  @Override
  public <T> T accept(ASTVisitor<T> visitor) {
    return visitor.visitAssignment(this);
  }
}
