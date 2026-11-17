/**
 * Reglas del formatter.
 *
 * <p>Cada regla implementa {@link org.printscript.formatter.FormattingRule} y decide el espacio de
 * las brechas del AST que le corresponden ({@link org.printscript.formatter.GapKind}). Para agregar
 * una regla configurable:
 *
 * <ol>
 *   <li>Escribir la clase de la regla en este paquete.
 *   <li>Registrarla con su clave en {@link org.printscript.formatter.rule.FormattingRules}, en el
 *       lugar que le corresponde según el orden en que se aplican.
 * </ol>
 *
 * <p>Con eso ya se puede activar desde el archivo de configuración: ni el formatter ni la lectura
 * del JSON cambian.
 */
package org.printscript.formatter.rule;
