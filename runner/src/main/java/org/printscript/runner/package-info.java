/**
 * Orquestación del lenguaje PrintScript: arma el pipeline lexer → parser → (semántico, linter,
 * intérprete, formatter) para cada operación.
 *
 * <p>Es el único módulo que sabe que el lenguaje tiene versiones: {@link
 * org.printscript.runner.LanguageVersion} le arma a cada módulo la gramática y las funciones
 * nativas de la versión pedida.
 *
 * <p>Este paquete es puro respecto de la interfaz de usuario: no imprime en consola ni accede al
 * sistema de archivos. Recibe el código fuente como {@link java.io.Reader} y devuelve resultados (o
 * emite salida a través de los providers inyectados). Así lo pueden consumir tanto la CLI como una
 * API HTTP sin duplicar lógica.
 */
package org.printscript.runner;
