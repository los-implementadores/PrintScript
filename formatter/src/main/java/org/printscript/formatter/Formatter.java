package org.printscript.formatter;

import java.io.Writer;
import java.util.Iterator;
import org.printscript.ast.Statement;
import org.printscript.source.SourceText;

/**
 * Formateador de código PrintScript.
 *
 * <p>Recorre el AST y escribe cada nodo tal como estaba en el código original. Entre cada par de
 * partes de un nodo hay una brecha ({@link Gap}) con el espacio original, y las reglas deciden qué
 * espacio queda. El espacio que ninguna regla toca queda como estaba (ver ADR-029).
 */
public interface Formatter {

  /**
   * Escribe el código formateado de a una sentencia, a medida que llegan.
   *
   * @param statements las sentencias del programa, en orden
   * @param source el código del que salieron las sentencias, para reproducir lo que el AST no
   *     guarda (los espacios)
   * @param target donde se escribe el código formateado
   * @throws java.io.UncheckedIOException si falla la escritura
   * @throws IllegalStateException si las sentencias no coinciden con el código original
   */
  void format(Iterator<Statement> statements, SourceText source, Writer target);
}
