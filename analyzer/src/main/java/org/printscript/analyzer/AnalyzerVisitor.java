package org.printscript.analyzer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.printscript.common.Position;
import org.printscript.common.ast.*;
import org.printscript.common.configs.AnalyzerConfig;
import org.printscript.common.configs.NamingConvention;
import org.printscript.common.env.Environment;
import org.printscript.common.linterViolations.Severity;
import org.printscript.common.linterViolations.Violation;

public class AnalyzerVisitor implements ASTVisitor<Void> {

  private final AnalyzerConfig config;
  private final List<Violation> violations = new ArrayList<>();

  private final Environment env = new Environment();
  private final List<String> declaredVariableNames = new ArrayList<>();

  // Clase auxiliar para guardar en el "value" del Environment
  private static class LinterMetadata {
    final Position position;
    boolean isUsed;

    LinterMetadata(Position position) {
      this.position = position;
      this.isUsed = false;
    }
  }

  public AnalyzerVisitor(AnalyzerConfig config) {
    this.config = config;
  }

  public List<Violation> getFinalViolations() {
    if (config.activeUnusedVariables) {
      for (String varName : declaredVariableNames) {
        LinterMetadata meta = (LinterMetadata) env.get(varName);
        if (!meta.isUsed) {
          violations.add(
              new Violation(
                  "Variable '" + varName + "' is declared but never used.",
                  Severity.WARNING,
                  meta.position));
        }
      }
    }
    return violations;
  }

  @Override
  public Void visitProgram(Program node) {
    Iterator<Statement> it = node.stream();
    while (it.hasNext()) {
      it.next().accept(this);
    }
    return null;
  }

  @Override
  public Void visitVarDeclaration(VarDeclarationStatement node) {
    String varName = node.getName().getName();
    Position position = node.getPosition();
    String typeName = node.getTypeName();

    // 1. Regla: Convención de nombres
    if (config.activeNamingConvention) {
      if (config.namingConventionFormat.equals(NamingConvention.CAMEL_CASE)
          && !varName.matches("^[a-z]+([A-Z][a-z0-9]+)*$")) {
        violations.add(
            new Violation(
                "Variable '" + varName + "' should be in camelCase.", Severity.WARNING, position));
      } else if (config.namingConventionFormat.equals(NamingConvention.SNAKE_CASE)
          && !varName.matches("^[a-z]+(_[a-z0-9]+)*$")) {
        violations.add(
            new Violation(
                "Variable '" + varName + "' should be in snake_case.", Severity.WARNING, position));
      }
    }

    // 2. Registramos la variable en el Environment
    if (!env.isDeclared(varName)) {
      env.define(varName, typeName, new LinterMetadata(position));
      declaredVariableNames.add(varName);
    }

    if (node.getInitializer() != null) {
      node.getInitializer().accept(this);
    }
    return null;
  }

  @Override
  public Void visitAssignment(AssignmentStatement node) {
    node.getValue().accept(this);
    String varName = node.getTarget().getName();

    // Registra el uso de la variable
    if (env.isDeclared(varName)) {
      LinterMetadata meta = (LinterMetadata) env.get(varName);
      meta.isUsed = true;
    }
    return null;
  }

  @Override
  public Void visitExpressionStatement(ExpressionStatement node) {
    node.getExpression().accept(this);
    return null;
  }

  @Override
  public Void visitBinaryExpression(BinaryExpression node) {
    node.getLeft().accept(this);
    node.getRight().accept(this);
    return null;
  }

  @Override
  public Void visitCallExpression(CallExpression node) {
    if (config.activeComplexPrintln
        && node.getCallee().equals("println")
        && !node.getArguments().isEmpty()) {
      Node argument = node.getArguments().get(0);
      boolean isLiteralOrIdentifier =
          argument instanceof NumberLiteral
              || argument instanceof StringLiteral
              || argument instanceof Identifier;

      if (!isLiteralOrIdentifier) {
        violations.add(
            new Violation(
                "Complex expression in 'println'. Consider extracting to an intermediate variable.",
                Severity.WARNING,
                node.getPosition()));
      }
    }

    for (Node arg : node.getArguments()) {
      if (arg instanceof Expression) arg.accept(this);
    }
    return null;
  }

  @Override
  public Void visitNumberLiteral(NumberLiteral node) {
    return null;
  }

  @Override
  public Void visitStringLiteral(StringLiteral node) {
    return null;
  }

  @Override
  public Void visitBooleanLiteral(BooleanLiteral node) {
    return null;
  }

  @Override
  public Void visitIdentifier(Identifier node) {
    String varName = node.getName();
    if (env.isDeclared(varName)) {
      LinterMetadata meta = (LinterMetadata) env.get(varName);
      meta.isUsed = true; // variable leida
    }
    return null;
  }
}
