package org.printscript.runner;

import java.io.Reader;
import org.printscript.common.LanguageVersion;
import org.printscript.formatter.FormattingRules;

/** Devuelve el código fuente formateado según las reglas. No escribe en ningún lado. */
public class FormattingService {

  private final ProgramLoader loader;
  private final FormatterFactory formatters;

  FormattingService(ProgramLoader loader, FormatterFactory formatters) {
    this.loader = loader;
    this.formatters = formatters;
  }

  public String format(Reader source, LanguageVersion version, FormattingRules rules) {
    return formatters.create(rules).format(loader.load(source, version));
  }
}
