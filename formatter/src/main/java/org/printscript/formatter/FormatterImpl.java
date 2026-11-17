package org.printscript.formatter;

import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;
import org.printscript.ast.Statement;
import org.printscript.formatter.rule.FormattingRules;
import org.printscript.rules.RuleConfig;
import org.printscript.source.SourceText;

/**
 * Escribe el programa con {@link FormatterVisitor} y le pasa cada brecha a las reglas, en el orden
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
   * <p>Recibe una fábrica y no la lista porque una regla propia puede guardar estado: cada formateo
   * arranca con reglas nuevas.
   */
  public FormatterImpl(Supplier<List<FormattingRule>> rules) {
    this.rules = rules;
  }

  @Override
  public void format(Iterator<Statement> statements, SourceText source, Writer target) {
    FormattedOutput out = new FormattedOutput(source, List.copyOf(rules.get()), target);
    new FormatterVisitor(out).writeProgram(statements);
  }
}
