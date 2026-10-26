package org.printscript.analyzer;

import java.util.List;
import java.util.function.Supplier;
import org.printscript.analyzer.config.AnalyzerConfig;
import org.printscript.analyzer.rule.LintRule;
import org.printscript.analyzer.violation.Violation;
import org.printscript.common.ast.Statement;

public class StaticAnalyzerImpl implements StaticAnalyzer {

  private final AnalyzerVisitor visitor;

  /** Usa las reglas que la configuración deja activas. */
  public StaticAnalyzerImpl(AnalyzerConfig config) {
    this.visitor = new AnalyzerVisitor(config);
  }

  /**
   * Usa las reglas que arma {@code rules}, incluidas reglas propias que no están en la config.
   *
   * <p>Recibe una fábrica y no la lista porque las reglas guardan estado (por ejemplo, qué
   * variables se usaron): cada analyzer le pide su propia lista, así dos análisis no se mezclan
   * aunque compartan la fábrica.
   */
  public StaticAnalyzerImpl(Supplier<List<LintRule>> rules) {
    this.visitor = new AnalyzerVisitor(rules.get());
  }

  @Override
  public void analyze(Statement statement) {
    statement.accept(visitor);
  }

  @Override
  public List<Violation> getViolations() {
    return visitor.getFinalViolations();
  }
}
