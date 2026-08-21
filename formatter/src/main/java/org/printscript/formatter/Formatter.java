package org.printscript.formatter;

import org.printscript.common.ast.Program;

/**
 * Formateador de código PrintScript.
 *
 * <p>Recibe un programa parseado (AST) y produce el código fuente reformateado según las reglas
 * configuradas.
 */
public interface Formatter {

  /**
   * Formatea un programa completo.
   *
   * @param program el AST del programa a formatear
   * @return el código fuente formateado como String
   */
  String format(Program program);
}
