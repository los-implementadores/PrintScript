package org.printscript.token;

public enum TokenType {
  // Keywords
  LET,
  CONST, // "const" declaración inmutable
  IF, // "if"
  ELSE, // "else"
  TYPE_NUMBER, // "number" usado como tipo en una declaración
  TYPE_STRING, // "string" usado como tipo en una declaración
  TYPE_BOOLEAN, // "boolean" usado como tipo en una declaración

  // Literales e identificadores
  IDENTIFIER,
  NUMBER_LITERAL,
  STRING_LITERAL, // el lexema incluye las comillas, tal como está en el código fuente
  BOOLEAN_LITERAL, // true / false

  // Símbolos
  COLON, // :
  ASSIGN, // =
  SEMICOLON, // ;
  LPAREN, // (
  RPAREN, // )
  LBRACE, // {
  RBRACE, // }
  PLUS, // +
  MINUS, // -
  STAR, // *
  SLASH, // /

  // Espacios, tabs y saltos de línea entre tokens. El lexer solo los produce si se le pide (lo usa
  // el formatter); el parser nunca los ve.
  WHITESPACE,

  EOF
}
