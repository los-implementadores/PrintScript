package org.printscript.lexer;

import org.junit.jupiter.api.Test;
import org.printscript.common.token.Token;
import org.printscript.common.token.TokenType;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LexerTest {

    private List<Token> tokenize(String source) {
        Lexer lexer = new LexerImpl(new StringReader(source));
        List<Token> tokens = new ArrayList<>();
        while (lexer.hasNext()) {
            tokens.add(lexer.next());
        }
        return tokens;
    }

    @Test
    void tokenizesStringDeclarationAndPrintln() {
        String source = "let name: string = \"Joe\";\n"
                + "let lastName: string = \"Doe\";\n"
                + "\n"
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

        // primer token: "let" en la fila 1, columnas 1 a 3
        Token first = tokens.get(0);
        assertEquals("let", first.getLexeme());
        assertEquals(1, first.getPosition().getStartLine());
        assertEquals(1, first.getPosition().getStartColumn());
        assertEquals(3, first.getPosition().getEndColumn());
    }

    @Test
    void tokenizesNumberDeclarationAndDivision() {
        String source = "let a: number = 12;\nlet b: number = 4;\nlet c: number = a / b;";

        List<Token> tokens = tokenize(source);

        assertEquals(TokenType.NUMBER_LITERAL, tokens.get(5).getType());
        assertEquals("12", tokens.get(5).getLexeme());
        assertEquals(TokenType.SLASH, tokens.get(20).getType());
    }

    @Test
    void supportsSingleAndDoubleQuotedStrings() {
        List<Token> tokens = tokenize("let x: string = 'hi';");
        assertEquals(TokenType.STRING_LITERAL, tokens.get(5).getType());
        assertEquals("hi", tokens.get(5).getLexeme());
    }

    @Test
    void throwsOnUnterminatedString() {
        assertThrows(LexerException.class, () -> tokenize("let x: string = \"oops;"));
    }

    @Test
    void throwsOnUnexpectedCharacter() {
        assertThrows(LexerException.class, () -> tokenize("let x: number = 1 @ 2;"));
    }
}
