package org.printscript.analyzer;

import java.util.List;
import org.printscript.analyzer.violation.Violation;
import org.printscript.common.ast.Statement;

public interface StaticAnalyzer {
  /** Analiza una sentencia individual y acumula violaciones internamente. */
  void analyze(Statement statement);

  /** Devuelve todas las violaciones encontradas tras analizar las sentencias. */
  List<Violation> getViolations();
}
