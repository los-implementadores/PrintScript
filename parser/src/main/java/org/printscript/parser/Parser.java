package org.printscript.parser;

import java.util.Iterator;
import org.printscript.common.ast.Statement;

/**
 * Analizador sintáctico de PrintScript.
 *
 * <p>Extiende {@link Iterator}{@code <Statement>}: produce una sentencia por llamada a {@link
 * #next()}, sin materializar el árbol completo.
 *
 * <p>El parser no conoce al lexer. Solo consume tokens de un {@code Iterator<Token>} que recibe en
 * su constructor. Es responsabilidad del orquestador (CLI) crear el lexer y pasarle el iterador de
 * tokens al parser.
 *
 * <p>Si el código fuente contiene un error sintáctico, {@link #next()} lanza una {@link
 * ParseException} con la posición exacta del token inesperado (fail-fast: se detiene en el primer
 * error).
 */
public interface Parser extends Iterator<Statement> {}
