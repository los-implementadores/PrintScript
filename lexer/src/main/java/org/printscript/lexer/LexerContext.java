package org.printscript.lexer;

import org.printscript.common.token.Token;
import org.printscript.common.token.TokenType;

/**
 * Contexto que el lexer expone a cada {@link TokenMatcher} para que pueda leer caracteres, avanzar
 * en el flujo y construir tokens.
 *
 * <p>Encapsula el estado interno del lexer (posición, caracter actual) y evita que los matchers
 * conozcan la implementación completa.
 */
public interface LexerContext {

  /** Caracter actual (el que está por ser consumido). Devuelve -1 si llegó al final. */
  int getCurrentChar();

  /** Fila actual en el código fuente (base 1). */
  int getCurrentLine();

  /** Columna actual en el código fuente (base 1). */
  int getCurrentColumn();

  /** Avanza al siguiente caracter, actualizando fila y columna. */
  void advance();

  /**
   * Construye un {@link Token} con la posición indicada.
   *
   * @param type tipo del token
   * @param lexeme texto original del token
   * @param startLine fila de inicio
   * @param startColumn columna de inicio
   * @param endLine fila de fin
   * @param endColumn columna de fin
   * @return el token construido
   */
  Token createToken(
      TokenType type, String lexeme, int startLine, int startColumn, int endLine, int endColumn);
}
