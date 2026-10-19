package org.printscript.runner;

import java.io.Reader;
import org.printscript.common.LanguageVersion;

/**
 * Valida sintaxis y semántica de un programa sin ejecutarlo. Falla con excepción ante el primer
 * error.
 */
public class ValidationService {

  private final ProgramLoader loader;
  private final SemanticAnalyzerFactory semanticAnalyzers;

  ValidationService(ProgramLoader loader, SemanticAnalyzerFactory semanticAnalyzers) {
    this.loader = loader;
    this.semanticAnalyzers = semanticAnalyzers;
  }

  public void validate(Reader source, LanguageVersion version) {
    StatementPipeline.of(semanticAnalyzers.create(version)::analyze)
        .run(loader.load(source, version));
  }
}
