package org.printscript.runner;

import java.io.Reader;
import java.io.Writer;
import org.printscript.formatter.rule.FormattingRules;
import org.printscript.rules.RuleConfig;

/**
 * Formatea un programa en streaming: parsea una sentencia, formatea sus tokens y los escribe antes
 * de pedir la siguiente, así nunca está el programa entero en memoria.
 */
public class FormattingService {

  private final TokenLoader loader;
  private final FormatterFactory formatters;

  FormattingService(TokenLoader loader, FormatterFactory formatters) {
    this.loader = loader;
    this.formatters = formatters;
  }

  /**
   * Un error de sintaxis corta la escritura a mitad de camino: quien escribe en un archivo tiene
   * que descartar lo escrito si este método falla.
   *
   * @param rules la configuración de las reglas de formato (ver {@link #defaultRules()})
   * @param target donde se escribe el código formateado, a medida que se produce
   */
  public void format(Reader source, LanguageVersion version, RuleConfig rules, Writer target) {
    formatters.create(rules).format(loader.load(source, version), target);
  }

  /** Las reglas que se usan cuando no se configura ninguna. */
  public RuleConfig defaultRules() {
    return FormattingRules.defaults();
  }
}
