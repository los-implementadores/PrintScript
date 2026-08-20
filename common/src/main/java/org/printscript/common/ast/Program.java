package org.printscript.common.ast;

import java.util.Iterator;
import java.util.List;

/**
 * Nodo raíz del AST: representa el programa completo como una secuencia de sentencias.
 *
 * <p>{@code Program} es una interfaz para permitir dos estrategias de materialización:
 *
 * <ul>
 *   <li>{@link EagerProgram} — carga todas las sentencias en una lista inmutable en el momento de
 *       construcción. Usar cuando se necesita el árbol completo (p.ej. el analyzer que hace
 *       múltiples pasadas).
 *   <li>{@link LazyProgram} — envuelve un {@code Iterator<Statement>} y drena el parser on-demand.
 *       Usar cuando se quiere minimizar el pico de memoria (p.ej. el interpreter que procesa de
 *       arriba a abajo una sola vez).
 * </ul>
 *
 * <h2>Métodos</h2>
 *
 * <ul>
 *   <li>{@link #toList()} — materializa y devuelve todas las sentencias como lista inmutable.
 *   <li>{@link #stream()} — devuelve un iterator lazy sobre las sentencias.
 * </ul>
 *
 * <p>Ambos métodos producen el mismo resultado lógico. La diferencia está en cuándo y cómo se
 * cargan las sentencias en memoria.
 */
public interface Program extends Node {

  /**
   * Devuelve todas las sentencias del programa como una lista inmutable.
   *
   * <p>En {@link LazyProgram}, la primera llamada drena el iterator y cachea el resultado. Llamadas
   * posteriores devuelven la misma lista ya cargada.
   *
   * <p>Usar cuando se necesita acceso aleatorio o múltiples pasadas sobre el programa (p.ej. el
   * analyzer).
   *
   * @return lista inmutable de sentencias en el orden del código fuente
   */
  List<Statement> toList();

  /**
   * Devuelve un iterator sobre las sentencias del programa.
   *
   * <p>En {@link LazyProgram}, el iterator drena el parser subyacente de a una sentencia por vez,
   * sin cargar el resto en memoria. Ideal para módulos que procesan el programa de arriba a abajo
   * una sola vez (p.ej. el interpreter).
   *
   * <p>En {@link EagerProgram}, el iterator recorre la lista ya cargada.
   *
   * @return iterator sobre las sentencias en el orden del código fuente
   */
  Iterator<Statement> stream();
}
