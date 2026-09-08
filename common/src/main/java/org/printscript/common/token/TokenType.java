package org.printscript.common.token;

public enum TokenType {
  // Keywords
  LET,
  CONST, // "const" declaración inmutable (1.1)
  TYPE_NUMBER, // "number" usado como tipo en una declaración
  TYPE_STRING, // "string" usado como tipo en una declaración
  TYPE_BOOLEAN, // "boolean" usado como tipo en una declaración (1.1)

  // Literales e identificadores
  IDENTIFIER,
  NUMBER_LITERAL,
  STRING_LITERAL,
  BOOLEAN_LITERAL, // true / false (1.1)

  // Símbolos
  COLON, // :
  ASSIGN, // =
  SEMICOLON, // ;
  LPAREN, // (
  RPAREN, // )
  PLUS, // +
  MINUS, // -
  STAR, // *
  SLASH, // /

  EOF
}
