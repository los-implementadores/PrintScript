package org.printscript.runner;

import java.io.Reader;
import java.util.Iterator;
import org.printscript.token.Token;

/** Convierte código fuente en los tokens que formatea el formatter, espacios incluidos. */
@FunctionalInterface
interface TokenLoader {

  Iterator<Token> load(Reader source, LanguageVersion version);
}
