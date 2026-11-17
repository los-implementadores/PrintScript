package org.printscript.formatter;

/**
 * Qué espacio es una brecha, en términos del lenguaje: no «el espacio antes de un {@code :}» sino
 * «el espacio antes del {@code :} de una declaración».
 *
 * <p>Cada nodo del AST tiene sus brechas, entre cada par de sus partes. Un nodo nuevo en el
 * lenguaje define las suyas, y si ninguna de las que hay le sirve, agrega una acá.
 */
public enum GapKind {

  /** Antes de la primera sentencia del archivo, o el archivo entero si no tiene sentencias. */
  FILE_START,

  /** Después de la última sentencia del archivo. */
  FILE_END,

  /** Entre dos sentencias seguidas, del archivo o de un bloque. */
  BETWEEN_STATEMENTS,

  /** Entre la llave que abre un bloque y su primera sentencia. */
  BLOCK_START,

  /**
   * Antes de la llave que cierra un bloque: después de su última sentencia, o de la que lo abre.
   */
  BLOCK_END,

  /** Después de una palabra reservada: {@code let x}, {@code const x}, {@code if (}. */
  AFTER_KEYWORD,

  /** Antes del {@code :} de una declaración: {@code let x : number}. */
  BEFORE_TYPE_COLON,

  /** Después del {@code :} de una declaración: {@code let x: number}. */
  AFTER_TYPE_COLON,

  /** Antes del {@code =} de una declaración o una asignación. */
  BEFORE_ASSIGN,

  /** Después del {@code =} de una declaración o una asignación. */
  AFTER_ASSIGN,

  /** Antes del operador de una operación: {@code a + b}. */
  BEFORE_OPERATOR,

  /** Después del operador de una operación: {@code a + b}. */
  AFTER_OPERATOR,

  /** Antes del {@code ;} que termina una sentencia. */
  BEFORE_SEMICOLON,

  /** Entre el nombre de una función y el paréntesis de sus argumentos: {@code println (x)}. */
  BEFORE_ARGUMENTS,

  /** Adentro de un par de paréntesis, pegado a ellos: en llamadas, agrupaciones y condiciones. */
  INSIDE_PARENS,

  /** Antes de la coma que separa dos argumentos. */
  BEFORE_COMMA,

  /** Después de la coma que separa dos argumentos. */
  AFTER_COMMA,

  /** Antes de la llave que abre el bloque de un {@code if} o de un {@code else}. */
  BEFORE_BLOCK,

  /** Entre la llave que cierra el bloque del {@code if} y el {@code else}. */
  BEFORE_ELSE
}
