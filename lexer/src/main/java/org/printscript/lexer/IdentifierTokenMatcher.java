package org.printscript.lexer;

import org.printscript.common.token.Token;
import org.printscript.common.token.TokenType;

import java.util.Collections;
import java.util.Map;

/**
 * Matcher para identificadores y palabras reservadas (keywords).
 *
 * <p>Reconoce secuencias que empiezan con letra o {@code _} y continúan con
 * letras, dígitos o {@code _}. Luego busca el lexema en el mapa de keywords:
 * si está, devuelve el tipo correspondiente; si no, devuelve {@code IDENTIFIER}.
 */
public class IdentifierTokenMatcher implements TokenMatcher {

    private final Map<String, TokenType> keywords;

    /**
     * @param keywords mapa de lexema → TokenType para las palabras reservadas
     */
    public IdentifierTokenMatcher(Map<String, TokenType> keywords) {
        this.keywords = Collections.unmodifiableMap(keywords);
    }

    @Override
    public boolean matches(char currentChar) {
        return Character.isLetter(currentChar) || currentChar == '_';
    }

    @Override
    public Token extract(LexerContext context) {
        int startLine = context.getCurrentLine();
        int startColumn = context.getCurrentColumn();

        StringBuilder sb = new StringBuilder();
        while (context.getCurrentChar() != -1
                && (Character.isLetterOrDigit((char) context.getCurrentChar())
                    || context.getCurrentChar() == '_')) {
            sb.append((char) context.getCurrentChar());
            context.advance();
        }

        String lexeme = sb.toString();
        TokenType type = keywords.getOrDefault(lexeme, TokenType.IDENTIFIER);
        return context.createToken(type, lexeme, startLine, startColumn,
                context.getCurrentLine(), context.getCurrentColumn() - 1);
    }
}
