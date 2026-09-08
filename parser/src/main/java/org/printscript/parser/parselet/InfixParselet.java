package org.printscript.parser.parselet;

import org.printscript.common.ast.Expression;
import org.printscript.common.token.Token;
import org.printscript.parser.ParseContext;

/**
 * Parsea una expresión que aparece en posición <em>infija</em>: un operador binario entre una
 * expresión izquierda ya parseada y una derecha por parsear (ej. {@code a + b}).
 *
 * <p>Cada operador lleva embebido su binding power (precedencia), reemplazando el {@code switch} de
 * precedencias por datos de cada parselet. Agregar un operador = registrar un parselet nuevo.
 */
public interface InfixParselet {

  /** Binding power (precedencia) del operador. Mayor valor = liga más fuerte. */
  int bindingPower();

  /**
   * @param left expresión izquierda ya parseada
   * @param token el token del operador ya consumido
   * @param ctx servicios de parseo (stream y recursión de expresiones)
   * @return la expresión infija resultante
   */
  Expression parse(Expression left, Token token, ParseContext ctx);
}
