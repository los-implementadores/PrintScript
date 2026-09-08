package org.printscript.parser;

import org.printscript.common.ast.Expression;
import org.printscript.common.ast.Statement;

/**
 * Servicios que un parselet necesita para hacer su trabajo, sin acoplarse a {@code ParserImpl}.
 *
 * <p>Expone el {@link TokenStream} (para consumir tokens) y la capacidad de parsear expresiones
 * (para recursión Pratt). Depender de esta interfaz —y no de la implementación concreta— respeta el
 * principio de inversión de dependencias (DIP).
 */
public interface ParseContext {

  /** Stream de tokens compartido. */
  TokenStream tokens();

  /**
   * Parsea una sentencia completa a partir del token actual, despachando al {@link
   * StatementParselet} correspondiente. Permite que un parselet compuesto (ej. {@code if} con
   * bloques) parsee sentencias anidadas sin acoplarse a {@code ParserImpl}.
   */
  Statement parseStatement();

  /**
   * Parsea una expresión completa respetando la precedencia (binding power) mínima indicada. Es el
   * punto de entrada recursivo del algoritmo de Pratt.
   */
  Expression parseExpression(int minBindingPower);

  /**
   * Continúa el parseo Pratt a partir de una expresión izquierda ya construida, aplicando los
   * operadores infijos cuyo binding power supere {@code minBindingPower}. Útil cuando el {@code
   * left} ya fue consumido por fuera (ej. un identificador al inicio de una sentencia).
   */
  Expression parseInfix(Expression left, int minBindingPower);
}
