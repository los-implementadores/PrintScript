package org.printscript.lexer;

import java.util.Iterator;
import org.printscript.common.token.Token;

/**
 * Analizador léxico de PrintScript.
 *
 * <p>Expone los tokens de a uno (Iterator) en vez de tokenizar todo el archivo de entrada de una:
 * así el parser puede empezar a consumir tokens sin esperar a que el lexer termine, y nunca hace
 * falta tener todo el archivo fuente en memoria.
 */
public interface Lexer extends Iterator<Token> {}
