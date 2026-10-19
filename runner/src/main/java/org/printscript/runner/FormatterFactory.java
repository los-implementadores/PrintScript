package org.printscript.runner;

import org.printscript.formatter.Formatter;
import org.printscript.formatter.FormattingRules;

/** Crea un formateador con las reglas dadas. */
@FunctionalInterface
interface FormatterFactory {

  Formatter create(FormattingRules rules);
}
