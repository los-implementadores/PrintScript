/**
 * Reglas del linter.
 *
 * <p>Hay dos clases de regla, según lo que necesitan ver, y cada una implementa solo lo suyo:
 *
 * <ul>
 *   <li>{@link org.printscript.analyzer.rule.NodeRule}: revisa los nodos de un tipo, de a uno. Es
 *       una función del nodo a sus violaciones.
 *   <li>{@link org.printscript.analyzer.rule.ProgramRule}: necesita el programa entero. Es un fold:
 *       recibe lo que sabía y una sentencia, y devuelve lo que sabe ahora.
 * </ul>
 *
 * <p>Ninguna regla guarda estado, así que la misma instancia sirve para cualquier cantidad de
 * análisis. Para agregar una regla configurable:
 *
 * <ol>
 *   <li>Escribir la clase de la regla en este paquete.
 *   <li>Registrarla con su clave en {@link org.printscript.analyzer.rule.LintRules}.
 * </ol>
 *
 * <p>Con eso ya se puede activar desde el archivo de configuración: ni el recorrido ni la lectura
 * del JSON cambian. Una regla que no viene de la configuración se le pasa directo a {@code
 * StaticAnalyzerImpl(LintRules)}.
 */
package org.printscript.analyzer.rule;
