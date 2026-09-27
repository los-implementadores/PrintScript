package org.printscript.cli.io;

/**
 * Único punto de contacto de la CLI con la terminal. Todo lo que se muestra o se lee del usuario
 * pasa por acá, así el resto de la CLI no toca {@code System.out}/{@code System.in}.
 */
public interface Console {

  void println(String line);

  void print(String text);

  void error(String line);

  /**
   * Lee una línea de la entrada estándar.
   *
   * @return la línea leída, o {@code null} si se alcanzó el fin de la entrada
   */
  String readLine();
}
