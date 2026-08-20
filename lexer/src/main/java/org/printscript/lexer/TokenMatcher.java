package org.printscript.lexer;

import org.printscript.common.token.Token;

/**
 * Estrategia para reconocer y extraer un tipo de token del flujo de caracteres.
 *
 * <p>Cada implementación es responsable de un tipo de token (identificadores, números, strings,
 * símbolos, etc.). El lexer itera la lista de matchers hasta que uno puede manejar el caracter
 * actual.
 *
 * <p>Para agregar un nuevo tipo de token al lenguaje, basta con crear una nueva implementación de
 * esta interfaz y agregarla a la lista — sin modificar el lexer (Open/Closed Principle).
 */
public interface TokenMatcher {

  /**
   * Determina si este matcher puede manejar el caracter actual.
   *
   * @param currentChar el caracter que el lexer está viendo (nunca -1/EOF)
   * @return {@code true} si este matcher puede producir un token a partir de ese caracter
   */
  boolean matches(char currentChar);

  /**
   * Extrae el token completo del flujo de caracteres.
   *
   * <p>Solo se invoca si {@link #matches(char)} devolvió {@code true}. La implementación debe
   * consumir todos los caracteres que forman el token usando los métodos de {@link LexerContext}.
   *
   * @param context acceso al estado del lexer (posición, avance, creación de tokens)
   * @return el token producido
   */
  Token extract(LexerContext context);
}
