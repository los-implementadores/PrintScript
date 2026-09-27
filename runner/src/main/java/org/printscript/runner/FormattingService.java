package org.printscript.runner;

import java.io.Reader;
import org.printscript.common.LanguageVersion;
import org.printscript.formatter.FormatterImpl;
import org.printscript.formatter.FormattingRules;

/** Devuelve el código fuente formateado según las reglas. No escribe en ningún lado. */
public class FormattingService {

  public String format(Reader source, LanguageVersion version, FormattingRules rules) {
    return new FormatterImpl(rules).format(ProgramLoader.load(source, version));
  }
}
