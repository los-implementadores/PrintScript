package org.printscript.analyzer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import org.printscript.analyzer.rule.LintRule;
import org.printscript.analyzer.rule.LintRules;
import org.printscript.analyzer.violation.Violation;
import org.printscript.ast.Node;
import org.printscript.ast.Statement;
import org.printscript.rules.RuleConfig;

/**
 * Recorre cada sentencia y le avisa a cada {@link LintRule} los nodos que visita. No conoce ninguna
 * regla ni ningún tipo de nodo en particular: recorre el árbol con {@link Node#children()}, así que
 * ni una regla nueva ni un nodo nuevo cambian esta clase.
 */
public class StaticAnalyzerImpl implements StaticAnalyzer {

  private final List<LintRule> rules;
  private final List<Violation> violations = new ArrayList<>();

  /**
   * Usa las reglas que la configuración deja activas.
   *
   * @throws IllegalArgumentException si una clave tiene un valor que su regla no acepta
   */
  public StaticAnalyzerImpl(RuleConfig config) {
    this(() -> LintRules.create(config));
  }

  /**
   * Usa las reglas que arma {@code rules}, incluidas reglas propias que no están en la config.
   *
   * <p>Recibe una fábrica y no la lista porque las reglas guardan estado (por ejemplo, qué
   * variables se usaron): cada analyzer le pide su propia lista, así dos análisis no se mezclan
   * aunque compartan la fábrica.
   */
  public StaticAnalyzerImpl(Supplier<List<LintRule>> rules) {
    this.rules = List.copyOf(rules.get());
  }

  @Override
  public void analyze(Statement statement) {
    walk(statement);
  }

  /**
   * Devuelve las violaciones encontradas hasta ahora, incluidas las de las reglas que reportan al
   * final. Se puede llamar más de una vez sin duplicar resultados.
   */
  @Override
  public List<Violation> getViolations() {
    List<Violation> result = new ArrayList<>(violations);
    for (LintRule rule : rules) {
      rule.onFinish(result::add);
    }
    return result;
  }

  private void walk(Node node) {
    for (LintRule rule : rules) {
      rule.onEnter(node, violations::add);
    }
    for (Node child : node.children()) {
      walk(child);
    }
    for (LintRule rule : rules) {
      rule.onExit(node, violations::add);
    }
  }
}
