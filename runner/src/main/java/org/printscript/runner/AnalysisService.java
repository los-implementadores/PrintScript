package org.printscript.runner;

import java.io.Reader;
import java.util.Iterator;
import java.util.List;
import org.printscript.analyzer.StaticAnalyzer;
import org.printscript.analyzer.StaticAnalyzerImpl;
import org.printscript.common.LanguageVersion;
import org.printscript.common.ast.Statement;
import org.printscript.common.configs.AnalyzerConfig;
import org.printscript.common.env.Environment;
import org.printscript.common.linterViolations.Violation;
import org.printscript.interpreter.semantic.SemanticAnalyzer;
import org.printscript.interpreter.semantic.SemanticAnalyzerImpl;

/**
 * Chequeo semántico estricto + linter. Los errores semánticos se lanzan como excepción; las reglas
 * del linter se devuelven como violaciones.
 */
public class AnalysisService {

  public List<Violation> analyze(Reader source, LanguageVersion version, AnalyzerConfig config) {
    SemanticAnalyzer semantic = new SemanticAnalyzerImpl(new Environment(), version);
    StaticAnalyzer linter = new StaticAnalyzerImpl(config);

    Iterator<Statement> statements = ProgramLoader.load(source, version).stream();
    while (statements.hasNext()) {
      Statement stmt = statements.next();
      semantic.analyze(stmt);
      linter.analyze(stmt);
    }
    return linter.getViolations();
  }
}
