package org.printscript.runner;

import org.printscript.analyzer.StaticAnalyzer;
import org.printscript.analyzer.config.AnalyzerConfig;

/** Crea un linter nuevo con las reglas dadas para cada programa. */
@FunctionalInterface
interface LinterFactory {

  StaticAnalyzer create(AnalyzerConfig config);
}
