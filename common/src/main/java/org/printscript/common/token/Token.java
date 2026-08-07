package org.printscript.common.token;

import org.printscript.common.Position;

import java.util.Objects;

public final class Token {

    private final TokenType type;
    private final String lexeme;
    private final Position position;

    public Token(TokenType type, String lexeme, Position position) {
        this.type = type;
        this.lexeme = lexeme;
        this.position = position;
    }

    public TokenType getType() {
        return type;
    }

    public String getLexeme() {
        return lexeme;
    }

    public Position getPosition() {
        return position;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Token)) return false;
        Token token = (Token) o;
        return type == token.type
                && Objects.equals(lexeme, token.lexeme)
                && Objects.equals(position, token.position);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, lexeme, position);
    }

    @Override
    public String toString() {
        return type + "('" + lexeme + "')@" + position;
    }
}
