package org.printscript.runner;

import org.printscript.analyzer.StaticAnalyzerImpl;
import org.printscript.common.env.Environment;
import org.printscript.formatter.FormatterImpl;
import org.printscript.interpreter.InterpreterImpl;
import org.printscript.interpreter.semantic.SemanticAnalyzerImpl;

/**
 * Los servicios del lenguaje, uno por operación.
 *
 * <p>{@link #printScript()} es el único lugar del módulo que elige implementaciones concretas
 * (lexer, parser, análisis semántico, intérprete, linter, formateador). Los servicios reciben esas
 * piezas por constructor y sólo conocen sus interfaces, así se pueden probar con dobles.
 */
public record LanguageServices(
    ValidationService validation,
    AnalysisService analysis,
    ExecutionService execution,
    FormattingService formatting) {

  /** Los servicios armados con las implementaciones de PrintScript. */
  public static LanguageServices printScript() {
    ProgramLoader loader = new LazyProgramLoader();
    SemanticAnalyzerFactory semanticAnalyzers =
        version -> new SemanticAnalyzerImpl(new Environment(), version);
    InterpreterFactory interpreters =
        (version, io) -> new InterpreterImpl(new Environment(), io, version);

    return new LanguageServices(
        new ValidationService(loader, semanticAnalyzers),
        new AnalysisService(loader, semanticAnalyzers, StaticAnalyzerImpl::new),
        new ExecutionService(loader, semanticAnalyzers, interpreters),
        new FormattingService(loader, FormatterImpl::new));
  }
}
