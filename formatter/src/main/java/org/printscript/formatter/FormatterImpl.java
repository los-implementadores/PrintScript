package org.printscript.formatter;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;
import org.printscript.formatter.rule.FormattingRules;
import org.printscript.rules.RuleConfig;
import org.printscript.token.Token;
import org.printscript.token.TokenType;

/**
 * Reescribe los tokens tal como están y le pasa cada espacio entre tokens a las reglas, en el orden
 * en que se registraron. No conoce ninguna regla en particular: agregar una no cambia esta clase.
 */
public class FormatterImpl implements Formatter {

  private final Supplier<List<FormattingRule>> rules;

  /**
   * Usa las reglas que la configuración deja activas. Si una clave tiene un valor que su regla no
   * acepta, {@link #format} falla con {@code IllegalArgumentException} antes de escribir nada.
   */
  public FormatterImpl(RuleConfig config) {
    this(() -> FormattingRules.create(config));
  }

  /**
   * Usa las reglas que arma {@code rules}, incluidas reglas propias que no están en la config.
   *
   * <p>Recibe una fábrica y no la lista porque las reglas pueden guardar estado (la profundidad de
   * llaves, por ejemplo): cada formateo arranca con reglas nuevas.
   */
  public FormatterImpl(Supplier<List<FormattingRule>> rules) {
    this.rules = rules;
  }

  @Override
  public void format(Iterator<Token> tokens, Writer target) {
    try {
      write(tokens, List.copyOf(rules.get()), target);
    } catch (IOException e) {
      throw new UncheckedIOException("Error al escribir código formateado", e);
    }
  }

  private static void write(Iterator<Token> tokens, List<FormattingRule> rules, Writer target)
      throws IOException {
    StringBuilder whitespace = new StringBuilder();
    Token previous = null;
    String lineIndent = "";
    while (tokens.hasNext()) {
      Token token = tokens.next();
      if (token.getType() == TokenType.WHITESPACE) {
        whitespace.append(token.getLexeme());
        continue;
      }
      Gap gap = new Gap(previous, token, whitespace.toString(), lineIndent);
      for (FormattingRule rule : rules) {
        gap = gap.withWhitespace(rule.apply(gap));
      }
      target.write(gap.whitespace());
      target.write(token.getLexeme());
      lineIndent = indentAfter(gap, lineIndent);
      previous = token;
      whitespace.setLength(0);
    }
    // Si los tokens no terminan en EOF, el espacio que quedó se escribe como estaba.
    target.write(whitespace.toString());
  }

  /** La indentación de la línea en la que quedó el token que sigue a {@code gap}. */
  private static String indentAfter(Gap gap, String lineIndent) {
    String written = gap.whitespace();
    int lastBreak = written.lastIndexOf('\n');
    if (lastBreak >= 0) {
      return written.substring(lastBreak + 1);
    }
    return gap.atStart() ? written : lineIndent;
  }
}
