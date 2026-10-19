package org.printscript.analyzer;

import java.util.List;
import org.printscript.analyzer.config.AnalyzerConfig;
import org.printscript.analyzer.violation.Violation;
import org.printscript.common.ast.Statement;

public class StaticAnalyzerImpl implements StaticAnalyzer {

  private final AnalyzerVisitor visitor;

  public StaticAnalyzerImpl(AnalyzerConfig config) {
    this.visitor = new AnalyzerVisitor(config);
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
