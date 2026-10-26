package org.printscript.runner;

import java.io.Reader;
import org.printscript.common.LanguageVersion;
import org.printscript.interpreter.ExecutionIo;
import org.printscript.interpreter.Interpreter;
import org.printscript.interpreter.semantic.SemanticAnalyzer;

/** Valida tipos y ejecuta un programa sentencia por sentencia (streaming). */
public class ExecutionService {

  private final ProgramLoader loader;
  private final SemanticAnalyzerFactory semanticAnalyzers;
  private final InterpreterFactory interpreters;

  ExecutionService(
      ProgramLoader loader,
      SemanticAnalyzerFactory semanticAnalyzers,
      InterpreterFactory interpreters) {
    this.loader = loader;
    this.semanticAnalyzers = semanticAnalyzers;
    this.interpreters = interpreters;
  }

  public void execute(Reader source, LanguageVersion version, ExecutionIo io) {
    SemanticAnalyzer semantic = semanticAnalyzers.create(version);
    Interpreter interpreter = interpreters.create(version, io);
    StatementPipeline.of(semantic::analyze, interpreter::execute).run(loader.load(source, version));
  }
}
