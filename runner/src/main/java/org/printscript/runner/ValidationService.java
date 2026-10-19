package org.printscript.runner;

import java.io.Reader;
import java.util.Iterator;
import org.printscript.common.LanguageVersion;
import org.printscript.common.ast.Statement;
import org.printscript.common.env.Environment;
import org.printscript.interpreter.semantic.SemanticAnalyzer;
import org.printscript.interpreter.semantic.SemanticAnalyzerImpl;

/**
 * Valida sintaxis y semántica de un programa sin ejecutarlo. Falla con excepción ante el primer
 * error.
 */
public class ValidationService {

  public void validate(Reader source, LanguageVersion version) {
    SemanticAnalyzer semantic = new SemanticAnalyzerImpl(new Environment(), version);
    Iterator<Statement> statements = ProgramLoader.load(source, version).stream();
    while (statements.hasNext()) {
      semantic.analyze(statements.next());
    }
  }
}
