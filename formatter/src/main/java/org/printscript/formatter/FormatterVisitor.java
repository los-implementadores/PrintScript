package org.printscript.formatter;

import java.util.List;
import org.printscript.common.ast.*;

/**
 * Visitor que recorre el AST y produce código fuente formateado según las {@link FormattingRules}.
 *
 * <p>Cada método {@code visit*} devuelve un {@code String} con el fragmento de código formateado
 * para ese nodo. El resultado final se compone concatenando los fragmentos.
 */
public class FormatterVisitor implements ASTVisitor<String> {

  private final FormattingRules rules;

  public FormatterVisitor(FormattingRules rules) {
    this.rules = rules;
  }

  @Override
  public String visitProgram(Program node) {
    List<Statement> statements = node.toList();
    StringBuilder sb = new StringBuilder();

    for (int i = 0; i < statements.size(); i++) {
      Statement stmt = statements.get(i);

      if (i > 0 && shouldAddExtraNewline(stmt)) {
        sb.append("\n".repeat(rules.getNewlineBeforePrintln()));
      }

      sb.append(stmt.accept(this));
      sb.append('\n');
    }

    return sb.toString();
  }

  @Override
  public String visitVarDeclaration(VarDeclarationStatement node) {
    StringBuilder sb = new StringBuilder();
    sb.append("let ");
    sb.append(node.getName().accept(this));
    sb.append(colonFormatted());
    sb.append(node.getTypeName());
    sb.append(assignFormatted());
    sb.append(node.getInitializer().accept(this));
    sb.append(';');
    return sb.toString();
  }

  @Override
  public String visitAssignment(AssignmentStatement node) {
    StringBuilder sb = new StringBuilder();
    sb.append(node.getTarget().accept(this));
    sb.append(assignFormatted());
    sb.append(node.getValue().accept(this));
    sb.append(';');
    return sb.toString();
  }

  @Override
  public String visitExpressionStatement(ExpressionStatement node) {
    return node.getExpression().accept(this) + ";";
  }

  @Override
  public String visitBinaryExpression(BinaryExpression node) {
    String left = node.getLeft().accept(this);
    String right = node.getRight().accept(this);
    String op = node.getOperator();

    if (rules.isSpaceAroundOperators()) {
      return left + " " + op + " " + right;
    }
    return left + op + right;
  }

  @Override
  public String visitCallExpression(CallExpression node) {
    StringBuilder sb = new StringBuilder();
    sb.append(node.getCallee());
    sb.append('(');
    List<Expression> args = node.getArguments();
    for (int i = 0; i < args.size(); i++) {
      if (i > 0) {
        sb.append(", ");
      }
      sb.append(args.get(i).accept(this));
    }
    sb.append(')');
    return sb.toString();
  }

  @Override
  public String visitNumberLiteral(NumberLiteral node) {
    double value = node.getValue();
    if (value % 1 == 0) {
      return String.valueOf((long) value);
    }
    return String.valueOf(value);
  }

  @Override
  public String visitStringLiteral(StringLiteral node) {
    return "\"" + node.getValue() + "\"";
  }

  @Override
  public String visitBooleanLiteral(BooleanLiteral node) {
    return String.valueOf(node.getValue());
  }

  @Override
  public String visitIdentifier(Identifier node) {
    return node.getName();
  }

  // ---------------------------------------------------------------- Helpers

  private String colonFormatted() {
    StringBuilder sb = new StringBuilder();
    if (rules.isSpaceBeforeColon()) {
      sb.append(' ');
    }
    sb.append(':');
    if (rules.isSpaceAfterColon()) {
      sb.append(' ');
    }
    return sb.toString();
  }

  private String assignFormatted() {
    if (rules.isSpaceAroundAssign()) {
      return " = ";
    }
    return "=";
  }

  private boolean shouldAddExtraNewline(Statement stmt) {
    if (!(stmt instanceof ExpressionStatement exprStmt)) {
      return false;
    }
    return exprStmt.getExpression() instanceof CallExpression;
  }
}
