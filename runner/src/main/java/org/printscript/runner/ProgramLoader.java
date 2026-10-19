package org.printscript.runner;

import java.io.Reader;
import org.printscript.common.LanguageVersion;
import org.printscript.common.ast.Program;

/** Convierte código fuente en un {@link Program}. */
@FunctionalInterface
interface ProgramLoader {

  Program load(Reader source, LanguageVersion version);
}
