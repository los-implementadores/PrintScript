package org.printscript.lexer;

import org.junit.jupiter.api.Test;
import org.printscript.common.token.Token;
import org.printscript.common.token.TokenType;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LexerTest {

    private static final List<TokenMatcher> MATCHERS = List.of(
            new IdentifierTokenMatcher(Map.of(
                    "let", TokenType.LET,
                    "number", TokenType.TYPE_NUMBER,
                    "string", TokenType.TYPE_STRING
            )),
            new NumberTokenMatcher(),
            new StringTokenMatcher(),
            new SymbolTokenMatcher(Map.of(
                    ':', TokenType.COLON,
                    '=', TokenType.ASSIGN,
                    ';', TokenType.SEMICOLON,
                    '(', TokenType.LPAREN,
                    ')', TokenType.RPAREN,
                    '+', TokenType.PLUS,
                    '-', TokenType.MINUS,
                    '*', TokenType.STAR,
                    '/', TokenType.SLASH
            ))
    );

    private List<Token> tokenize(String source) {
        Lexer lexer = new LexerImpl(new StringReader(source), MATCHERS);
        List<Token> tokens = new ArrayList<>();
        while (lexer.hasNext()) {
            tokens.add(lexer.next());
        }
        return tokens;
    }

    // ---------------------------------------------------------------- Identifiers & Keywords

    @Test
    void tokenizesIdentifier() {
        List<Token> tokens = tokenize("myVar;");
        assertEquals(TokenType.IDENTIFIER, tokens.get(0).getType());
        assertEquals("myVar", tokens.get(0).getLexeme());
    }

    @Test
    void tokenizesIdentifierWithUnderscore() {
        List<Token> tokens = tokenize("_count;");
        assertEquals(TokenType.IDENTIFIER, tokens.get(0).getType());
        assertEquals("_count", tokens.get(0).getLexeme());
    }

    @Test
    void tokenizesKeywordLet() {
        List<Token> tokens = tokenize("let;");
        assertEquals(TokenType.LET, tokens.get(0).getType());
        assertEquals("let", tokens.get(0).getLexeme());
    }

    @Test
    void tokenizesKeywordNumber() {
        List<Token> tokens = tokenize("number;");
        assertEquals(TokenType.TYPE_NUMBER, tokens.get(0).getType());
    }

    @Test
    void tokenizesKeywordString() {
        List<Token> tokens = tokenize("string;");
        assertEquals(TokenType.TYPE_STRING, tokens.get(0).getType());
    }

    @Test
    void identifierStartingWithKeywordIsNotKeyword() {
        List<Token> tokens = tokenize("letters;");
        assertEquals(TokenType.IDENTIFIER, tokens.get(0).getType());
        assertEquals("letters", tokens.get(0).getLexeme());
    }

    // ---------------------------------------------------------------- Numbers

    @Test
    void tokenizesInteger() {
        List<Token> tokens = tokenize("42;");
        assertEquals(TokenType.NUMBER_LITERAL, tokens.get(0).getType());
        assertEquals("42", tokens.get(0).getLexeme());
    }

    @Test
    void tokenizesDecimal() {
        List<Token> tokens = tokenize("3.14;");
        assertEquals(TokenType.NUMBER_LITERAL, tokens.get(0).getType());
        assertEquals("3.14", tokens.get(0).getLexeme());
    }

    // ---------------------------------------------------------------- Strings

    @Test
    void tokenizesDoubleQuotedString() {
        List<Token> tokens = tokenize("\"hello\";");
        assertEquals(TokenType.STRING_LITERAL, tokens.get(0).getType());
        assertEquals("hello", tokens.get(0).getLexeme());
    }

    @Test
    void tokenizesSingleQuotedString() {
        List<Token> tokens = tokenize("'world';");
        assertEquals(TokenType.STRING_LITERAL, tokens.get(0).getType());
        assertEquals("world", tokens.get(0).getLexeme());
    }

    @Test
    void throwsOnUnterminatedString() {
        assertThrows(LexerException.class, () -> tokenize("\"oops"));
    }

    // ---------------------------------------------------------------- Symbols

    @Test
    void tokenizesAllSymbols() {
        List<Token> tokens = tokenize(": = ; ( ) + - * /");
        List<TokenType> types = new ArrayList<>();
        for (Token t : tokens) {
            types.add(t.getType());
        }
        assertEquals(List.of(
                TokenType.COLON, TokenType.ASSIGN, TokenType.SEMICOLON,
                TokenType.LPAREN, TokenType.RPAREN,
                TokenType.PLUS, TokenType.MINUS, TokenType.STAR, TokenType.SLASH,
                TokenType.EOF
        ), types);
    }

    // ---------------------------------------------------------------- Errores

    @Test
    void throwsOnUnexpectedCharacter() {
        LexerException ex = assertThrows(LexerException.class,
                () -> tokenize("x @ y;"));
        assertNotNull(ex.getPosition());
    }

    // ---------------------------------------------------------------- Posiciones

    @Test
    void tracksPositionAcrossLines() {
        List<Token> tokens = tokenize("let x: number = 1;\nlet y: number = 2;");

        Token secondLet = tokens.get(7); // segundo "let"
        assertEquals("let", secondLet.getLexeme());
        assertEquals(2, secondLet.getPosition().getStartLine());
        assertEquals(1, secondLet.getPosition().getStartColumn());
    }

    @Test
    void tracksColumnPositionOfKeyword() {
        List<Token> tokens = tokenize("let x: number = 5;");
        Token first = tokens.get(0);
        assertEquals(1, first.getPosition().getStartLine());
        assertEquals(1, first.getPosition().getStartColumn());
        assertEquals(3, first.getPosition().getEndColumn());
    }

    // ---------------------------------------------------------------- Programa completo

    @Test
    void tokenizesFullProgram() {
        String source = "let name: string = \"Joe\";\n"
                + "let lastName: string = \"Doe\";\n"
                + "println(name + \" \" + lastName);";

        List<Token> tokens = tokenize(source);

        List<TokenType> types = new ArrayList<>();
        for (Token t : tokens) {
            types.add(t.getType());
        }

        assertEquals(List.of(
                TokenType.LET, TokenType.IDENTIFIER, TokenType.COLON, TokenType.TYPE_STRING,
                TokenType.ASSIGN, TokenType.STRING_LITERAL, TokenType.SEMICOLON,

                TokenType.LET, TokenType.IDENTIFIER, TokenType.COLON, TokenType.TYPE_STRING,
                TokenType.ASSIGN, TokenType.STRING_LITERAL, TokenType.SEMICOLON,

                TokenType.IDENTIFIER, TokenType.LPAREN,
                TokenType.IDENTIFIER, TokenType.PLUS, TokenType.STRING_LITERAL,
                TokenType.PLUS, TokenType.IDENTIFIER,
                TokenType.RPAREN, TokenType.SEMICOLON,

                TokenType.EOF
        ), types);
    }

    // ---------------------------------------------------------------- Extensibilidad

    @Test
    void supportsCustomMatcherWithoutModifyingLexer() {
        // Demuestra OCP: agregamos soporte para '!' sin tocar LexerImpl ni los matchers existentes
        List<TokenMatcher> extendedMatchers = List.of(
                new IdentifierTokenMatcher(Map.of("let", TokenType.LET)),
                new NumberTokenMatcher(),
                new StringTokenMatcher(),
                new SymbolTokenMatcher(Map.of(
                        ';', TokenType.SEMICOLON,
                        '!', TokenType.IDENTIFIER  // se usa IDENTIFIER como placeholder
                ))
        );

        Lexer lexer = new LexerImpl(new StringReader("!;"), extendedMatchers);
        Token bang = lexer.next();
        assertEquals("!", bang.getLexeme());
    }
}
