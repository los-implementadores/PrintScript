package org.printscript.interpreter;

/**
 * Abstracción inyectable para la provisión de entradas en tiempo de ejecución.
 *
 * <p>Permite desacoplar el intérprete de la fuente real de entrada (ej. {@code System.in} en la
 * CLI, o fuentes programáticas/mockeadas para testing y ejecuciones desatendidas).
 */
@FunctionalInterface
public interface InputProvider {

  /**
   * Solicita y lee una entrada del usuario o ambiente.
   *
   * @param prompt mensaje que se imprimió previamente al usuario
   * @return línea o valor ingresado como texto, o {@code null} si se alcanzó el fin de la entrada
   */
  String readInput(String prompt);
}
