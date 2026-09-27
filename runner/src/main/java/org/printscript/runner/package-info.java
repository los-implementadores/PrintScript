/**
 * Orquestación del lenguaje PrintScript: arma el pipeline lexer → parser → (semántico, linter,
 * intérprete, formatter) para cada operación.
 *
 * <p>Este paquete es puro respecto de la interfaz de usuario: no imprime en consola ni accede al
 * sistema de archivos. Recibe el código fuente como {@link java.io.Reader} y devuelve resultados (o
 * emite salida a través de los providers inyectados). Así lo pueden consumir tanto la CLI como una
 * API HTTP sin duplicar lógica.
 */
package org.printscript.runner;
