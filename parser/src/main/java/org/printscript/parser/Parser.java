package org.printscript.parser;

import org.printscript.common.ast.Program;
import org.printscript.lexer.Lexer;

/**
 * Analizador sintáctico de PrintScript.
 *
 * Consume los tokens producidos por un {@link Lexer} y construye el AST
 * representado como un {@link Program}.
 *
 * <p>Uso típico:
 * <pre>
 *   Lexer lexer = new LexerImpl(new StringReader(source));
 *   Parser parser = new ParserImpl(lexer);
 *   Program program = parser.parse();
 * </pre>
 *
 * <p>Si el código fuente contiene un error sintáctico, {@link #parse()} lanza
 * una {@link ParseException} con la posición exacta del token inesperado
 * (fail-fast: se detiene en el primer error).
 */
public interface Parser {

    /**
     * Parsea el stream de tokens completo y devuelve el AST raíz.
     *
     * @return el {@link Program} que representa el programa completo
     * @throws ParseException si se encuentra un token inesperado o falta un token requerido
     */
    Program parse();
}
