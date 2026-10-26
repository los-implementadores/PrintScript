package org.printscript.runner;

import java.io.Reader;
import java.io.Writer;
import org.printscript.common.LanguageVersion;
import org.printscript.formatter.FormattingRules;

/**
 * Formatea un programa en streaming: parsea una sentencia, la formatea y la escribe antes de pedir
 * la siguiente, así nunca está el programa entero en memoria.
 */
public class FormattingService {

  private final ProgramLoader loader;
  private final FormatterFactory formatters;

  FormattingService(ProgramLoader loader, FormatterFactory formatters) {
    this.loader = loader;
    this.formatters = formatters;
  }

  /**
   * Un error de sintaxis corta la escritura a mitad de camino: quien escribe en un archivo tiene
   * que descartar lo escrito si este método falla.
   *
   * @param target donde se escribe el código formateado, a medida que se produce
   */
  public void format(Reader source, LanguageVersion version, FormattingRules rules, Writer target) {
    formatters.create(rules).format(loader.load(source, version), target);
  }
}
