package org.printscript.runner;

import java.io.Reader;
import org.printscript.common.LanguageVersion;

/** Valida la sintaxis de un programa. Falla con excepción ante el primer error. */
public class ValidationService {

  private final ProgramLoader loader;

  ValidationService(ProgramLoader loader) {
    this.loader = loader;
  }

  public void validate(Reader source, LanguageVersion version) {
    // Sin etapas: alcanza con que cada sentencia se pueda parsear.
    StatementPipeline.of().run(loader.load(source, version));
  }
}
