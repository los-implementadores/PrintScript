/**
 * Reglas del linter.
 *
 * <p>Cada regla implementa {@link org.printscript.analyzer.rule.LintRule} y mira solo los nodos que
 * le interesan. Para agregar una regla configurable:
 *
 * <ol>
 *   <li>Escribir la clase de la regla en este paquete.
 *   <li>Registrarla con su clave en {@link org.printscript.analyzer.rule.LintRules}.
 * </ol>
 *
 * <p>Con eso ya se puede activar desde el archivo de configuración: ni el recorrido ni la lectura
 * del JSON cambian. Una regla que no viene de la configuración se le puede pasar directo a {@code
 * StaticAnalyzerImpl(Supplier<List<LintRule>>)}. Es una fábrica y no una lista porque las reglas
 * guardan estado: cada análisis necesita las suyas.
 */
package org.printscript.analyzer.rule;
