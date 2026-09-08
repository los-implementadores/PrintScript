package org.printscript.parser.parselet;

import org.printscript.common.ast.Statement;
import org.printscript.parser.ParseContext;

/**
 * Parsea una sentencia completa (ej. una declaración {@code let ...;}).
 *
 * <p>Se registra por el {@code TokenType} que inicia la sentencia. Reemplaza la cadena de {@code
 * if} de {@code parseStatement()} por un lookup en un mapa: agregar una sentencia = registrar un
 * parselet, sin tocar el despachador (Open/Closed).
 */
public interface StatementParselet {

  /**
   * @param ctx servicios de parseo (stream y recursión de expresiones)
   * @return la sentencia parseada
   */
  Statement parse(ParseContext ctx);
}
