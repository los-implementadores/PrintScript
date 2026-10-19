package org.printscript.runner;

import org.printscript.common.LanguageVersion;
import org.printscript.interpreter.ExecutionIo;
import org.printscript.interpreter.Interpreter;

/** Crea un intérprete nuevo (con su memoria vacía) para cada ejecución. */
@FunctionalInterface
interface InterpreterFactory {

  Interpreter create(LanguageVersion version, ExecutionIo io);
}
