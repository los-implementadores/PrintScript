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
  STRING_LITERAL,
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

  EOF
}
