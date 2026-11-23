package org.printscript.runner;

import java.io.Reader;
import java.util.List;
import org.printscript.analyzer.StaticAnalyzer;
import org.printscript.analyzer.violation.Violation;
import org.printscript.ast.Program;
import org.printscript.interpreter.semantic.SemanticAnalyzer;
import org.printscript.rules.RuleConfig;

/**
 * Chequeo semántico estricto + linter. Los errores semánticos se lanzan como excepción; las reglas
 * del linter se devuelven como violaciones. El semántico chequea cada sentencia justo antes de que
 * la lea el linter, así el programa nunca está entero en memoria.
 */
public class AnalysisService {

  private final ProgramLoader loader;
  private final SemanticAnalyzerFactory semanticAnalyzers;
  private final LinterFactory linters;

  AnalysisService(
      ProgramLoader loader, SemanticAnalyzerFactory semanticAnalyzers, LinterFactory linters) {
    this.loader = loader;
    this.semanticAnalyzers = semanticAnalyzers;
    this.linters = linters;
  }

  /**
   * @param rules la configuración de las reglas del linter; sin claves, no aplica ninguna
   */
  public List<Violation> analyze(Reader source, LanguageVersion version, RuleConfig rules) {
    SemanticAnalyzer semantic = semanticAnalyzers.create(version);
    StaticAnalyzer linter = linters.create(rules);
    Program program = loader.load(source, version);
    return linter.analyze(StatementPipeline.of(semantic::analyze).stream(program));
  }
}
