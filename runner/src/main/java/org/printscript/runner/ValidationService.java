package org.printscript.runner;

import java.io.Reader;
import java.util.Iterator;
import org.printscript.common.LanguageVersion;
import org.printscript.common.ast.Statement;

/** Valida la sintaxis de un programa. Falla con excepción ante el primer error. */
public class ValidationService {

  public void validate(Reader source, LanguageVersion version) {
    Iterator<Statement> statements = ProgramLoader.load(source, version).stream();
    while (statements.hasNext()) {
      statements.next();
    }
  }
}
