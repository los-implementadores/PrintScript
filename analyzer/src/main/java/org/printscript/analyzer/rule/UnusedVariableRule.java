package org.printscript.analyzer.rule;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.printscript.analyzer.violation.Severity;
import org.printscript.analyzer.violation.Violation;
import org.printscript.common.Position;
import org.printscript.common.ast.AssignmentStatement;
import org.printscript.common.ast.Identifier;
import org.printscript.common.ast.VarDeclarationStatement;

/**
 * Reporta las variables declaradas que nunca se leen ni se reasignan. Como necesita haber visto
 * todo el programa, reporta recién en {@link #onFinish}.
 *
 * <p>Respeta los scopes de bloque: un uso cuenta para la declaración visible más cercana, así que
 * una variable de afuera tapada por otra del mismo nombre adentro de un bloque no cuenta como
 * usada.
 */
public final class UnusedVariableRule implements LintRule {

  /** Todas las declaraciones, en orden de declaración. */
  private final List<Declaration> declarations = new ArrayList<>();

  /** Declaraciones visibles por scope; el primero es el del bloque más interno. */
  private final Deque<Map<String, Declaration>> scopes = new ArrayDeque<>();

  public UnusedVariableRule() {
    scopes.push(new HashMap<>());
  }

  @Override
  public void onDeclaration(VarDeclarationStatement node, Consumer<Violation> report) {
    Map<String, Declaration> scope = scopes.peek();
    String name = node.getName().getName();
    if (!scope.containsKey(name)) {
      Declaration declaration = new Declaration(name, node.getPosition());
      scope.put(name, declaration);
      declarations.add(declaration);
    }
  }

  @Override
  public void onAssignment(AssignmentStatement node, Consumer<Violation> report) {
    markUsed(node.getTarget().getName());
  }

  @Override
  public void onIdentifier(Identifier node, Consumer<Violation> report) {
    markUsed(node.getName());
  }

  @Override
  public void onBlockStart() {
    scopes.push(new HashMap<>());
  }

  @Override
  public void onBlockEnd() {
    scopes.pop();
  }

  @Override
  public void onFinish(Consumer<Violation> report) {
    for (Declaration declaration : declarations) {
      if (!declaration.used) {
        report.accept(
            new Violation(
                "Variable '" + declaration.name + "' is declared but never used.",
                Severity.WARNING,
                declaration.position));
      }
    }
  }

  private void markUsed(String name) {
    for (Map<String, Declaration> scope : scopes) {
      Declaration declaration = scope.get(name);
      if (declaration != null) {
        declaration.used = true;
        return;
      }
    }
  }

  private static final class Declaration {
    private final String name;
    private final Position position;
    private boolean used;

    Declaration(String name, Position position) {
      this.name = name;
      this.position = position;
    }
  }
}
