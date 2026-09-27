package org.printscript.formatter;

import java.io.Reader;
import java.io.Writer;
import java.util.Iterator;
import org.printscript.common.ast.Program;

/**
 * Formateador de código PrintScript.
 *
 * <p>Soporta dos estrategias complementarias:
 *
 * <ul>
 *   <li><b>AST pretty-printer:</b> recibe un {@link Program} y regenera el código completo de forma
 *       canónica (útil para normalización total).
 *   <li><b>Streaming / Rule-based:</b> procesa el código fuente bajo demanda desde un {@link
 *       Reader} con memoria constante O(1), aplicando reglas específicas y preservando el resto del
 *       código.
 * </ul>
 */
public interface Formatter {

  /**
   * Formatea un programa a partir de su AST.
   *
   * @param program el AST del programa a formatear
   * @return el código fuente formateado como String
   */
  String format(Program program);

  /**
   * Formatea código fuente en streaming desde un {@link Reader} hacia un {@link Writer}.
   *
   * @param reader fuente de caracteres de entrada
   * @param writer destino donde escribir el código formateado
   */
  void format(Reader reader, Writer writer);

  /**
   * Formatea una cadena de código fuente aplicando las reglas configuradas.
   *
   * @param source código fuente original
   * @return código fuente formateado
   */
  String format(String source);

  /**
   * Devuelve un iterador que produce líneas formateadas a medida que se leen.
   *
   * @param reader fuente de caracteres
   * @return iterador de líneas formateadas con memoria O(1)
   */
  Iterator<String> formatIterator(Reader reader);
}
