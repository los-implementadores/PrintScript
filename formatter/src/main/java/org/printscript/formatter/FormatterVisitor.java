package org.printscript.formatter;

import java.util.Iterator;
import java.util.List;
import org.printscript.ast.ASTVisitor;
import org.printscript.ast.AssignmentStatement;
import org.printscript.ast.BinaryExpression;
import org.printscript.ast.Block;
import org.printscript.ast.BooleanLiteral;
import org.printscript.ast.CallExpression;
import org.printscript.ast.Expression;
import org.printscript.ast.ExpressionStatement;
import org.printscript.ast.GroupExpression;
import org.printscript.ast.Identifier;
import org.printscript.ast.IfStatement;
import org.printscript.ast.Node;
import org.printscript.ast.NumberLiteral;
import org.printscript.ast.Statement;
import org.printscript.ast.StringLiteral;
import org.printscript.ast.VarDeclarationStatement;

/**
 * Escribe cada nodo del AST con sus partes en orden y, entre cada par, una brecha que deciden las
 * reglas. Es el {@code Rewrite} de rustfmt: un método por nodo, que sabe qué partes tiene.
 *
 * <p>Las partes salen del AST (los nombres, los literales tal como se escribieron, las palabras
 * reservadas y los símbolos de cada construcción) y los espacios, del código original, que {@link
 * FormattedOutput} recorre en paralelo. Por eso, sin reglas, la salida es idéntica a la entrada. Si
 * el AST y el código no coinciden, falla en vez de escribir otro programa.
 */
final class FormatterVisitor implements ASTVisitor<Void> {

  private final FormattedOutput out;

  FormatterVisitor(FormattedOutput out) {
    this.out = out;
  }

  /** Escribe el programa de a una sentencia: pide la siguiente recién después de escribir esta. */
  void writeProgram(Iterator<Statement> statements) {
    Statement previous = null;
    while (statements.hasNext()) {
      Statement statement = statements.next();
      GapKind kind = previous == null ? GapKind.FILE_START : GapKind.BETWEEN_STATEMENTS;
      out.gap(kind, statement, previous);
      statement.accept(this);
      previous = statement;
    }
    out.gap(previous == null ? GapKind.FILE_START : GapKind.FILE_END, null, previous);
    out.finish();
  }

  // ---------------------------------------------------------------- Sentencias

  @Override
  public Void visitVarDeclaration(VarDeclarationStatement node) {
    out.text(node.isConst() ? "const" : "let");
    gap(GapKind.AFTER_KEYWORD, node);
    node.getName().accept(this);
    gap(GapKind.BEFORE_TYPE_COLON, node);
    out.text(":");
    gap(GapKind.AFTER_TYPE_COLON, node);
    out.text(node.getTypeName());
    if (node.getInitializer() != null) {
      assign(node, node.getInitializer());
    }
    semicolon(node);
    return null;
  }

  @Override
  public Void visitAssignment(AssignmentStatement node) {
    node.getTarget().accept(this);
    assign(node, node.getValue());
    semicolon(node);
    return null;
  }

  @Override
  public Void visitExpressionStatement(ExpressionStatement node) {
    node.getExpression().accept(this);
    semicolon(node);
    return null;
  }

  @Override
  public Void visitIf(IfStatement node) {
    out.text("if");
    gap(GapKind.AFTER_KEYWORD, node);
    parenthesized(node, node.getCondition());
    gap(GapKind.BEFORE_BLOCK, node);
    node.getThenBlock().accept(this);
    if (node.hasElse()) {
      gap(GapKind.BEFORE_ELSE, node);
      out.text("else");
      gap(GapKind.BEFORE_BLOCK, node);
      node.getElseBlock().accept(this);
    }
    return null;
  }

  @Override
  public Void visitBlock(Block node) {
    out.text("{");
    out.enterBlock();
    Statement previous = null;
    for (Statement statement : node.getStatements()) {
      GapKind kind = previous == null ? GapKind.BLOCK_START : GapKind.BETWEEN_STATEMENTS;
      out.gap(kind, statement, previous);
      statement.accept(this);
      previous = statement;
    }
    out.exitBlock();
    out.gap(GapKind.BLOCK_END, node, previous);
    out.text("}");
    return null;
  }

  // ---------------------------------------------------------------- Expresiones

  @Override
  public Void visitBinaryExpression(BinaryExpression node) {
    node.getLeft().accept(this);
    gap(GapKind.BEFORE_OPERATOR, node);
    out.text(node.getOperator());
    gap(GapKind.AFTER_OPERATOR, node);
    node.getRight().accept(this);
    return null;
  }

  @Override
  public Void visitCallExpression(CallExpression node) {
    out.text(node.getCallee());
    gap(GapKind.BEFORE_ARGUMENTS, node);
    out.text("(");
    gap(GapKind.INSIDE_PARENS, node);
    List<Expression> arguments = node.getArguments();
    for (int i = 0; i < arguments.size(); i++) {
      if (i > 0) {
        gap(GapKind.BEFORE_COMMA, node);
        out.text(",");
        gap(GapKind.AFTER_COMMA, node);
      }
      arguments.get(i).accept(this);
    }
    if (!arguments.isEmpty()) {
      gap(GapKind.INSIDE_PARENS, node);
    }
    out.text(")");
    return null;
  }

  @Override
  public Void visitGroup(GroupExpression node) {
    parenthesized(node, node.getExpression());
    return null;
  }

  @Override
  public Void visitNumberLiteral(NumberLiteral node) {
    out.text(node.getLexeme());
    return null;
  }

  @Override
  public Void visitStringLiteral(StringLiteral node) {
    out.text(node.getLexeme());
    return null;
  }

  @Override
  public Void visitBooleanLiteral(BooleanLiteral node) {
    out.text(String.valueOf(node.getValue()));
    return null;
  }

  @Override
  public Void visitIdentifier(Identifier node) {
    out.text(node.getName());
    return null;
  }

  // ---------------------------------------------------------------- Partes compartidas

  private void gap(GapKind kind, Node node) {
    out.gap(kind, node, null);
  }

  private void assign(Node node, Expression value) {
    gap(GapKind.BEFORE_ASSIGN, node);
    out.text("=");
    gap(GapKind.AFTER_ASSIGN, node);
    value.accept(this);
  }

  private void semicolon(Statement node) {
    gap(GapKind.BEFORE_SEMICOLON, node);
    out.text(";");
  }

  private void parenthesized(Node node, Expression inner) {
    out.text("(");
    gap(GapKind.INSIDE_PARENS, node);
    inner.accept(this);
    gap(GapKind.INSIDE_PARENS, node);
    out.text(")");
  }
}
