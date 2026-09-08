package org.printscript.parser.parselet;

import org.printscript.common.ast.Expression;
import org.printscript.common.token.Token;
import org.printscript.parser.ParseContext;

/**
 * Parsea una expresión que aparece en posición <em>prefija</em> (al inicio de una expresión): un
 * literal, un identificador, un paréntesis de agrupación, etc.
 *
 * <p>Cada tipo de expresión primaria es un {@code PrefixParselet} propio y se registra por {@code
 * TokenType}. Agregar una nueva forma prefija = agregar una clase + registrarla, sin tocar el
 * despachador (Open/Closed).
 */
public interface PrefixParselet {

  /**
   * @param token el token prefijo ya consumido del stream
   * @param ctx servicios de parseo (stream y recursión de expresiones)
   * @return la expresión parseada
   */
  Expression parse(Token token, ParseContext ctx);
}
