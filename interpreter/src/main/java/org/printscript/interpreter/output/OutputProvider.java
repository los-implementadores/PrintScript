package org.printscript.interpreter.output;

/**
 * Abstracción inyectable para la salida producida por el programa ({@code println} y los prompts de
 * {@code readInput}).
 *
 * <p>Permite desacoplar el intérprete de {@code System.out}: la CLI imprime a consola, mientras que
 * una API o un test pueden acumular las líneas y devolverlas como resultado.
 */
@FunctionalInterface
public interface OutputProvider {

  /**
   * Emite una línea de salida del programa.
   *
   * @param line texto a emitir, sin salto de línea final
   */
  void print(String line);
}
