/**
 * Reglas del linter.
 *
 * <p>Cada regla implementa {@link org.printscript.analyzer.rule.LintRule} y mira solo los nodos que
 * le interesan. Para agregar una regla configurable:
 *
 * <ol>
 *   <li>Escribir la clase de la regla en este paquete.
 *   <li>Agregar su clave en {@link org.printscript.analyzer.config.AnalyzerConfig}.
 *   <li>Registrarla en {@link org.printscript.analyzer.rule.LintRules#from}.
 * </ol>
 *
 * <p>Una regla que no viene de la configuración se le puede pasar directo a {@code
 * StaticAnalyzerImpl(List<LintRule>)}.
 */
package org.printscript.analyzer.rule;
