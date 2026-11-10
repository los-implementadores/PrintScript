package org.printscript.formatter;

import java.io.Writer;
import java.util.Iterator;
import org.printscript.token.Token;

/**
 * Formateador de código PrintScript.
 *
 * <p>Trabaja sobre los tokens del código fuente con sus espacios (los de {@code
 * LexerFactory.createWithWhitespace}): reescribe cada token tal como está y deja que las reglas
 * decidan qué espacio va entre cada par. El espacio que ninguna regla toca queda como estaba.
 *
 * <p>No usa el AST: un tipo de nodo nuevo en el lenguaje no cambia el formatter (ver ADR-027).
 */
public interface Formatter {

  /**
   * Escribe el código formateado a medida que lee los tokens: en memoria solo está el token actual.
   *
   * @param tokens los tokens del código fuente, espacios incluidos
   * @param target donde se escribe el código formateado
   * @throws java.io.UncheckedIOException si falla la escritura
   */
  void format(Iterator<Token> tokens, Writer target);
}
