package org.printscript.lexer;

import org.printscript.common.token.Token;
import org.printscript.common.token.TokenType;

/**
 * Matcher para literales numéricos (enteros y decimales).
 *
 * <p>Reconoce secuencias que empiezan con un dígito. Soporta un punto decimal
 * opcional seguido de más dígitos (e.g. {@code 3.14}).
 */
public class NumberTokenMatcher implements TokenMatcher {

    @Override
    public boolean matches(char currentChar) {
        return Character.isDigit(currentChar);
    }

    @Override
    public Token extract(LexerContext context) {
        int startLine = context.getCurrentLine();
        int startColumn = context.getCurrentColumn();

        StringBuilder sb = new StringBuilder();
        while (context.getCurrentChar() != -1 && Character.isDigit((char) context.getCurrentChar())) {
            sb.append((char) context.getCurrentChar());
            context.advance();
        }

        if (context.getCurrentChar() == '.') {
            sb.append('.');
            context.advance();
            while (context.getCurrentChar() != -1 && Character.isDigit((char) context.getCurrentChar())) {
                sb.append((char) context.getCurrentChar());
                context.advance();
            }
        }

        return context.createToken(TokenType.NUMBER_LITERAL, sb.toString(),
                startLine, startColumn, context.getCurrentLine(), context.getCurrentColumn() - 1);
    }
}
