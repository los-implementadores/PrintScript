package org.printscript.analyzer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.printscript.analyzer.config.AnalyzerConfig;
import org.printscript.analyzer.rule.LintRule;
import org.printscript.analyzer.rule.LintRules;
import org.printscript.analyzer.violation.Violation;
import org.printscript.common.ast.ASTVisitor;
import org.printscript.common.ast.AssignmentStatement;
import org.printscript.common.ast.BinaryExpression;
import org.printscript.common.ast.BooleanLiteral;
import org.printscript.common.ast.CallExpression;
import org.printscript.common.ast.Expression;
import org.printscript.common.ast.ExpressionStatement;
import org.printscript.common.ast.Identifier;
import org.printscript.common.ast.IfStatement;
import org.printscript.common.ast.NumberLiteral;
import org.printscript.common.ast.Program;
import org.printscript.common.ast.Statement;
import org.printscript.common.ast.StringLiteral;
import org.printscript.common.ast.VarDeclarationStatement;

/**
 * Recorre el AST y le avisa a cada {@link LintRule} los nodos que visita. No conoce ninguna regla
 * en particular: agregar una no cambia esta clase.
 */
public class AnalyzerVisitor implements ASTVisitor<Void> {

  private final List<LintRule> rules;
  private final List<Violation> violations = new ArrayList<>();

  /**
   * @param rules reglas de este análisis. Las reglas guardan estado, así que cada visitor necesita
   *     su propia lista (por eso {@link StaticAnalyzerImpl} recibe una fábrica).
   */
  public AnalyzerVisitor(List<LintRule> rules) {
    this.rules = List.copyOf(rules);
  }

  public AnalyzerVisitor(AnalyzerConfig config) {
    this(LintRules.from(config));
  }

  /**
   * Devuelve las violaciones encontradas hasta ahora, incluidas las de las reglas que reportan al
   * final. Se puede llamar más de una vez sin duplicar resultados.
   */
  public List<Violation> getFinalViolations() {
    List<Violation> result = new ArrayList<>(violations);
    for (LintRule rule : rules) {
      rule.onFinish(result::add);
    }
    return result;
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
    for (LintRule rule : rules) {
      rule.onDeclaration(node, violations::add);
    }
    if (node.getInitializer() != null) {
      node.getInitializer().accept(this);
    }
    return null;
  }

  @Override
  public Void visitAssignment(AssignmentStatement node) {
    node.getValue().accept(this);
    for (LintRule rule : rules) {
      rule.onAssignment(node, violations::add);
    }
    return null;
  }

  @Override
  public Void visitExpressionStatement(ExpressionStatement node) {
    node.getExpression().accept(this);
    return null;
  }

  @Override
  public Void visitIf(IfStatement node) {
    node.getCondition().accept(this);
    for (Statement stmt : node.getThenBlock()) {
      stmt.accept(this);
    }
    if (node.hasElse()) {
      for (Statement stmt : node.getElseBlock()) {
        stmt.accept(this);
      }
    }
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
    for (LintRule rule : rules) {
      rule.onCall(node, violations::add);
    }
    for (Expression arg : node.getArguments()) {
      arg.accept(this);
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
    for (LintRule rule : rules) {
      rule.onIdentifier(node, violations::add);
    }
    return null;
  }
}
