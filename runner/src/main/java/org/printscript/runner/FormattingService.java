package org.printscript.runner;

import java.io.Reader;
import java.io.Writer;
import org.printscript.ast.Program;
import org.printscript.formatter.Formatter;
import org.printscript.formatter.rule.FormattingRules;
import org.printscript.rules.RuleConfig;
import org.printscript.source.SourceText;

/**
 * Formatea un programa en streaming: parsea una sentencia, la escribe formateada y recién después
 * pide la siguiente, así nunca está el programa entero en memoria.
 *
 * <p>El formatter recibe las sentencias del AST y el código original, que se va guardando a medida
 * que lo lee el lexer ({@link SourceText#recording}) para reproducir los espacios que el AST no
 * guarda (ver ADR-029).
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
   * @param rules la configuración de las reglas de formato (ver {@link #defaultRules()})
   * @param target donde se escribe el código formateado, a medida que se produce
   */
  public void format(Reader source, LanguageVersion version, RuleConfig rules, Writer target) {
    Formatter formatter = formatters.create(rules);
    SourceText original = SourceText.recording(source);
    Program program = loader.load(original.reader(), version);
    formatter.format(program.stream(), original, target);
  }

  /** Las reglas que se usan cuando no se configura ninguna. */
  public RuleConfig defaultRules() {
    return FormattingRules.defaults();
  }
}
