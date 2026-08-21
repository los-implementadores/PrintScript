package org.printscript.formatter;

import org.printscript.common.ast.Program;

/**
 * Implementación de {@link Formatter} que delega en un {@link FormatterVisitor}.
 *
 * <p>Recibe las {@link FormattingRules} por constructor (inyección de dependencias). Para cambiar
 * las reglas, se crea una nueva instancia — sin modificar la clase (OCP).
 */
public class FormatterImpl implements Formatter {

  private final FormattingRules rules;

  public FormatterImpl(FormattingRules rules) {
    this.rules = rules;
  }

  @Override
  public String format(Program program) {
    FormatterVisitor visitor = new FormatterVisitor(rules);
    return program.accept(visitor);
  }
}
