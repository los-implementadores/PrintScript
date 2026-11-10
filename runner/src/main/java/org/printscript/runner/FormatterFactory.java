package org.printscript.runner;

import org.printscript.formatter.Formatter;
import org.printscript.rules.RuleConfig;

/** Crea un formateador con las reglas que activa la configuración. */
@FunctionalInterface
interface FormatterFactory {

  Formatter create(RuleConfig rules);
}
