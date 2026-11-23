package org.printscript.analyzer.rule;

import org.printscript.ast.ASTVisitor;
import org.printscript.ast.AssignmentStatement;
import org.printscript.ast.BinaryExpression;
import org.printscript.ast.Block;
import org.printscript.ast.BooleanLiteral;
import org.printscript.ast.CallExpression;
import org.printscript.ast.ExpressionStatement;
import org.printscript.ast.GroupExpression;
import org.printscript.ast.Identifier;
import org.printscript.ast.IfStatement;
import org.printscript.ast.NumberLiteral;
import org.printscript.ast.StringLiteral;
import org.printscript.ast.VarDeclarationStatement;

/**
 * {@code true} si una expresión es un literal o una variable. Los paréntesis de afuera no cuentan:
 * {@code (x)} sigue siendo una variable. Cada nodo responde por sí mismo (double dispatch), así que
 * no hay que preguntar de qué tipo es.
 */
final class LiteralOrVariable implements ASTVisitor<Boolean> {

  static final LiteralOrVariable INSTANCE = new LiteralOrVariable();

  private LiteralOrVariable() {}

  @Override
  public Boolean visitNumberLiteral(NumberLiteral node) {
    return true;
  }

  @Override
  public Boolean visitStringLiteral(StringLiteral node) {
    return true;
  }

  @Override
  public Boolean visitBooleanLiteral(BooleanLiteral node) {
    return true;
  }

  @Override
  public Boolean visitIdentifier(Identifier node) {
    return true;
  }

  @Override
  public Boolean visitGroup(GroupExpression node) {
    return node.getExpression().accept(this);
  }

  @Override
  public Boolean visitBinaryExpression(BinaryExpression node) {
    return false;
  }

  @Override
  public Boolean visitCallExpression(CallExpression node) {
    return false;
  }

  // Las sentencias no son expresiones: nunca son un argumento.

  @Override
  public Boolean visitVarDeclaration(VarDeclarationStatement node) {
    return false;
  }

  @Override
  public Boolean visitAssignment(AssignmentStatement node) {
    return false;
  }

  @Override
  public Boolean visitExpressionStatement(ExpressionStatement node) {
    return false;
  }

  @Override
  public Boolean visitIf(IfStatement node) {
    return false;
  }

  @Override
  public Boolean visitBlock(Block node) {
    return false;
  }
}
