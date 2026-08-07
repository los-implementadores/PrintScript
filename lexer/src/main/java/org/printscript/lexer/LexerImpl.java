package org.printscript.lexer;

import org.printscript.common.Position;
import org.printscript.common.token.Token;
import org.printscript.common.token.TokenType;

import java.io.IOException;
import java.io.Reader;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Implementación de {@link Lexer} basada en un {@link Reader}.
 *
 * Lee un caracter a la vez y arma un token por vez bajo demanda (ver {@link #next()}),
 * nunca vuelca el archivo entero a memoria.
 */
public class LexerImpl implements Lexer {

    private static final Set<String> KEYWORDS = Set.of("let", "number", "string");

    private final Reader reader;

    private int currentChar;
    private int currentLine = 1;
    private int currentColumn = 1;
    private boolean finished = false;

    public LexerImpl(Reader reader) {
        this.reader = reader;
        this.currentChar = rawRead();
    }

    @Override
    public boolean hasNext() {
        return !finished;
    }

    @Override
    public Token next() {
        if (finished) {
            throw new NoSuchElementException("No more tokens");
        }

        skipWhitespace();

        int startLine = currentLine;
        int startColumn = currentColumn;

        if (currentChar == -1) {
            finished = true;
            return token(TokenType.EOF, "", startLine, startColumn, startLine, startColumn);
        }

        char c = (char) currentChar;

        if (Character.isLetter(c) || c == '_') {
            return readIdentifierOrKeyword(startLine, startColumn);
        }
        if (Character.isDigit(c)) {
            return readNumber(startLine, startColumn);
        }
        if (c == '"' || c == '\'') {
            return readString(startLine, startColumn, c);
        }

        switch (c) {
            case ':':
                advance();
                return token(TokenType.COLON, ":", startLine, startColumn, startLine, startColumn);
            case '=':
                advance();
                return token(TokenType.ASSIGN, "=", startLine, startColumn, startLine, startColumn);
            case ';':
                advance();
                return token(TokenType.SEMICOLON, ";", startLine, startColumn, startLine, startColumn);
            case '(':
                advance();
                return token(TokenType.LPAREN, "(", startLine, startColumn, startLine, startColumn);
            case ')':
                advance();
                return token(TokenType.RPAREN, ")", startLine, startColumn, startLine, startColumn);
            case '+':
                advance();
                return token(TokenType.PLUS, "+", startLine, startColumn, startLine, startColumn);
            case '-':
                advance();
                return token(TokenType.MINUS, "-", startLine, startColumn, startLine, startColumn);
            case '*':
                advance();
                return token(TokenType.STAR, "*", startLine, startColumn, startLine, startColumn);
            case '/':
                advance();
                return token(TokenType.SLASH, "/", startLine, startColumn, startLine, startColumn);
            default:
                Position position = new Position(startLine, startColumn, startLine, startColumn);
                throw new LexerException("Unexpected character '" + c + "'", position);
        }
    }

    private Token readIdentifierOrKeyword(int startLine, int startColumn) {
        StringBuilder sb = new StringBuilder();
        while (currentChar != -1 && (Character.isLetterOrDigit((char) currentChar) || currentChar == '_')) {
            sb.append((char) currentChar);
            advance();
        }
        String lexeme = sb.toString();
        TokenType type = keywordType(lexeme);
        return token(type, lexeme, startLine, startColumn, currentLine, currentColumn - 1);
    }

    private TokenType keywordType(String lexeme) {
        if (!KEYWORDS.contains(lexeme)) {
            return TokenType.IDENTIFIER;
        }
        switch (lexeme) {
            case "let":
                return TokenType.LET;
            case "number":
                return TokenType.TYPE_NUMBER;
            case "string":
                return TokenType.TYPE_STRING;
            default:
                throw new IllegalStateException("Unhandled keyword: " + lexeme);
        }
    }

    private Token readNumber(int startLine, int startColumn) {
        StringBuilder sb = new StringBuilder();
        while (currentChar != -1 && Character.isDigit((char) currentChar)) {
            sb.append((char) currentChar);
            advance();
        }
        if (currentChar == '.') {
            sb.append('.');
            advance();
            while (currentChar != -1 && Character.isDigit((char) currentChar)) {
                sb.append((char) currentChar);
                advance();
            }
        }
        return token(TokenType.NUMBER_LITERAL, sb.toString(), startLine, startColumn, currentLine, currentColumn - 1);
    }

    private Token readString(int startLine, int startColumn, char quote) {
        advance(); // consume la comilla de apertura
        StringBuilder sb = new StringBuilder();
        while (currentChar != -1 && currentChar != quote) {
            sb.append((char) currentChar);
            advance();
        }
        if (currentChar == -1) {
            Position position = new Position(startLine, startColumn, currentLine, currentColumn - 1);
            throw new LexerException("Unterminated string literal", position);
        }
        int endLine = currentLine;
        int endColumn = currentColumn;
        advance(); // consume la comilla de cierre
        return token(TokenType.STRING_LITERAL, sb.toString(), startLine, startColumn, endLine, endColumn);
    }

    private void skipWhitespace() {
        while (currentChar == ' ' || currentChar == '\t' || currentChar == '\r' || currentChar == '\n') {
            advance();
        }
    }

    private void advance() {
        if (currentChar == '\n') {
            currentLine++;
            currentColumn = 1;
        } else {
            currentColumn++;
        }
        currentChar = rawRead();
    }

    private int rawRead() {
        try {
            return reader.read();
        } catch (IOException e) {
            throw new LexerException("I/O error reading source: " + e.getMessage(),
                    new Position(currentLine, currentColumn, currentLine, currentColumn));
        }
    }

    private Token token(TokenType type, String lexeme, int startLine, int startColumn, int endLine, int endColumn) {
        return new Token(type, lexeme, new Position(startLine, startColumn, endLine, endColumn));
    }
}
