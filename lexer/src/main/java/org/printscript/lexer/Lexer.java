package org.printscript.lexer;

import org.printscript.common.token.Token;

import java.util.Iterator;

/**
 * Analizador léxico de PrintScript.
 *
 * Expone los tokens de a uno (Iterator) en vez de tokenizar todo el archivo de
 * entrada de una: así el parser puede empezar a consumir tokens sin esperar a
 * que el lexer termine, y nunca hace falta tener todo el archivo fuente en memoria.
 */
public interface Lexer extends Iterator<Token> {
}
