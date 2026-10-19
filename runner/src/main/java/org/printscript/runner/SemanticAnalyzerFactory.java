package org.printscript.runner;

import org.printscript.common.LanguageVersion;
import org.printscript.interpreter.semantic.SemanticAnalyzer;

/** Crea un analizador semántico nuevo (con su tabla de símbolos vacía) para cada programa. */
@FunctionalInterface
interface SemanticAnalyzerFactory {

  SemanticAnalyzer create(LanguageVersion version);
}
