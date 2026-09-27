package org.printscript.runner;

import java.io.Reader;
import java.util.Iterator;
import org.printscript.common.LanguageVersion;
import org.printscript.common.ast.Statement;
import org.printscript.common.env.Environment;
import org.printscript.interpreter.Interpreter;
import org.printscript.interpreter.InterpreterImpl;
import org.printscript.interpreter.semantic.SemanticAnalyzer;
import org.printscript.interpreter.semantic.SemanticAnalyzerImpl;

/** Valida tipos y ejecuta un programa sentencia por sentencia (streaming). */
public class ExecutionService {

  public void execute(Reader source, LanguageVersion version, ExecutionIo io) {
    SemanticAnalyzer semantic = new SemanticAnalyzerImpl(new Environment(), version);
    Interpreter interpreter =
        new InterpreterImpl(new Environment(), io.input(), io.env(), io.output(), version);

    Iterator<Statement> statements = ProgramLoader.load(source, version).stream();
    while (statements.hasNext()) {
      Statement stmt = statements.next();
      semantic.analyze(stmt);
      interpreter.execute(stmt);
    }
  }
}
