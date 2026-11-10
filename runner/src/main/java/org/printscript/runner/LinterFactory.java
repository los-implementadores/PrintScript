package org.printscript.runner;

import org.printscript.analyzer.StaticAnalyzer;
import org.printscript.rules.RuleConfig;

/** Crea un linter nuevo con las reglas que activa la configuración, para cada programa. */
@FunctionalInterface
interface LinterFactory {

  StaticAnalyzer create(RuleConfig rules);
}
