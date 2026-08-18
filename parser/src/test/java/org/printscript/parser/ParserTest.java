package org.printscript.parser;

import org.junit.jupiter.api.Test;
import org.printscript.common.Position;
import org.printscript.common.ast.*;
import org.printscript.common.token.TokenType;
import org.printscript.lexer.*;

import java.io.StringReader;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ParserTest {

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

    private Program parse(String source) {
        LexerImpl lexer = new LexerImpl(new StringReader(source), MATCHERS);
        Parser parser = new ParserImpl(lexer);
        Position start = new Position(1, 1, 1, 1);
        return new LazyProgram(parser, start);
    }

    /** Parsea y materializa la lista de sentencias. */
    private List<Statement> parseStatements(String source) {
        return parse(source).toList();
    }

    // ---------------------------------------------------------------- VarDeclaration

    @Test
    void parsesNumberDeclaration() {
        List<Statement> stmts = parseStatements("let x: number = 42;");

        assertEquals(1, stmts.size());
        VarDeclarationStatement stmt = (VarDeclarationStatement) stmts.get(0);

        assertEquals("x", stmt.getName().getName());
        assertEquals("number", stmt.getTypeName());
        assertEquals(42.0, ((NumberLiteral) stmt.getInitializer()).getValue());
    }

    @Test
    void parsesStringDeclaration() {
        List<Statement> stmts = parseStatements("let name: string = \"Joe\";");

        VarDeclarationStatement stmt = (VarDeclarationStatement) stmts.get(0);
        assertEquals("name", stmt.getName().getName());
        assertEquals("string", stmt.getTypeName());
        assertEquals("Joe", ((StringLiteral) stmt.getInitializer()).getValue());
    }

    // ---------------------------------------------------------------- Assignment

    @Test
    void parsesAssignment() {
        List<Statement> stmts = parseStatements("let x: number = 1;\nx = 99;");

        assertEquals(2, stmts.size());
        AssignmentStatement assign = (AssignmentStatement) stmts.get(1);

        assertEquals("x", assign.getTarget().getName());
        assertEquals(99.0, ((NumberLiteral) assign.getValue()).getValue());
    }

    // ---------------------------------------------------------------- println

    @Test
    void parsesPrintlnWithIdentifier() {
        List<Statement> stmts = parseStatements("println(x);");

        ExpressionStatement stmt = (ExpressionStatement) stmts.get(0);
        CallExpression call = (CallExpression) stmt.getExpression();

        assertEquals("println", call.getCallee());
        assertEquals(1, call.getArguments().size());
        assertEquals("x", ((Identifier) call.getArguments().get(0)).getName());
    }

    @Test
    void parsesPrintlnWithStringLiteral() {
        List<Statement> stmts = parseStatements("println(\"hello\");");

        ExpressionStatement stmt = (ExpressionStatement) stmts.get(0);
        CallExpression call = (CallExpression) stmt.getExpression();
        assertEquals("hello", ((StringLiteral) call.getArguments().get(0)).getValue());
    }

    // ---------------------------------------------------------------- Pratt: precedencia

    @Test
    void respectsMultiplicationOverAddition() {
        // 2 + 3 * 4 debe parsear como 2 + (3 * 4)
        List<Statement> stmts = parseStatements("let r: number = 2 + 3 * 4;");

        VarDeclarationStatement stmt = (VarDeclarationStatement) stmts.get(0);
        BinaryExpression top = (BinaryExpression) stmt.getInitializer();

        assertEquals("+", top.getOperator());
        assertEquals(2.0, ((NumberLiteral) top.getLeft()).getValue());
        BinaryExpression right = (BinaryExpression) top.getRight();
        assertEquals("*", right.getOperator());
        assertEquals(3.0, ((NumberLiteral) right.getLeft()).getValue());
        assertEquals(4.0, ((NumberLiteral) right.getRight()).getValue());
    }

    @Test
    void respectsDivisionOverSubtraction() {
        // 10 - 6 / 2 debe parsear como 10 - (6 / 2)
        List<Statement> stmts = parseStatements("let r: number = 10 - 6 / 2;");

        VarDeclarationStatement stmt = (VarDeclarationStatement) stmts.get(0);
        BinaryExpression top = (BinaryExpression) stmt.getInitializer();

        assertEquals("-", top.getOperator());
        assertEquals("/", ((BinaryExpression) top.getRight()).getOperator());
    }

    @Test
    void leftAssociativityForAddition() {
        // 1 + 2 + 3 debe parsear como (1 + 2) + 3
        List<Statement> stmts = parseStatements("let r: number = 1 + 2 + 3;");

        VarDeclarationStatement stmt = (VarDeclarationStatement) stmts.get(0);
        BinaryExpression top = (BinaryExpression) stmt.getInitializer();

        assertEquals("+", top.getOperator());
        BinaryExpression left = (BinaryExpression) top.getLeft();
        assertEquals("+", left.getOperator());
        assertEquals(1.0, ((NumberLiteral) left.getLeft()).getValue());
        assertEquals(2.0, ((NumberLiteral) left.getRight()).getValue());
        assertEquals(3.0, ((NumberLiteral) top.getRight()).getValue());
    }

    @Test
    void parenthesesOverridePrecedence() {
        // (2 + 3) * 4 debe parsear como (2 + 3) * 4
        List<Statement> stmts = parseStatements("let r: number = (2 + 3) * 4;");

        VarDeclarationStatement stmt = (VarDeclarationStatement) stmts.get(0);
        BinaryExpression top = (BinaryExpression) stmt.getInitializer();

        assertEquals("*", top.getOperator());
        assertEquals("+", ((BinaryExpression) top.getLeft()).getOperator());
    }

    // ---------------------------------------------------------------- Programa completo

    @Test
    void parsesFullProgram() {
        String source =
                "let name: string = \"Joe\";\n" +
                "let lastName: string = \"Doe\";\n" +
                "println(name + \" \" + lastName);";

        List<Statement> stmts = parseStatements(source);
        assertEquals(3, stmts.size());
        assertInstanceOf(VarDeclarationStatement.class, stmts.get(0));
        assertInstanceOf(VarDeclarationStatement.class, stmts.get(1));
        assertInstanceOf(ExpressionStatement.class,     stmts.get(2));
    }

    @Test
    void parsesNumberProgramWithDivision() {
        String source =
                "let a: number = 12;\n" +
                "let b: number = 4;\n" +
                "let c: number = a / b;\n" +
                "println(c);";

        List<Statement> stmts = parseStatements(source);
        assertEquals(4, stmts.size());

        VarDeclarationStatement cDecl = (VarDeclarationStatement) stmts.get(2);
        BinaryExpression div = (BinaryExpression) cDecl.getInitializer();
        assertEquals("/", div.getOperator());
        assertEquals("a", ((Identifier) div.getLeft()).getName());
        assertEquals("b", ((Identifier) div.getRight()).getName());
    }

    // ---------------------------------------------------------------- LazyProgram: stream()

    @Test
    void parserAsIteratorProducesStatementsLazily() {
        String source = "let a: number = 1;\nlet b: number = 2;";
        LexerImpl lexer = new LexerImpl(new StringReader(source), MATCHERS);
        Parser parser = new ParserImpl(lexer);
        Position start = new Position(1, 1, 1, 1);
        Program program = new LazyProgram(parser, start);

        // consume de a uno via stream(), sin materializar toList()
        java.util.Iterator<Statement> it = program.stream();
        assertTrue(it.hasNext());
        assertInstanceOf(VarDeclarationStatement.class, it.next());
        assertTrue(it.hasNext());
        assertInstanceOf(VarDeclarationStatement.class, it.next());
        assertFalse(it.hasNext());
    }

    // ---------------------------------------------------------------- Posiciones

    @Test
    void tracksPositionOfVarDeclaration() {
        List<Statement> stmts = parseStatements("let x: number = 5;");

        VarDeclarationStatement stmt = (VarDeclarationStatement) stmts.get(0);
        assertEquals(1, stmt.getPosition().getStartLine());
        assertEquals(1, stmt.getPosition().getStartColumn());
    }

    // ---------------------------------------------------------------- Errores

    @Test
    void throwsOnMissingColon() {
        assertThrows(ParseException.class, () -> parseStatements("let x number = 5;"));
    }

    @Test
    void throwsOnMissingSemicolon() {
        assertThrows(ParseException.class, () -> parseStatements("let x: number = 5"));
    }

    @Test
    void throwsOnUnknownType() {
        assertThrows(ParseException.class, () -> parseStatements("let x: boolean = true;"));
    }

    @Test
    void throwsOnUnexpectedToken() {
        assertThrows(ParseException.class, () -> parseStatements("let x: number = ;"));
    }

    @Test
    void parseExceptionContainsPosition() {
        ParseException ex = assertThrows(ParseException.class,
                () -> parseStatements("let x: number = ;"));
        assertNotNull(ex.getPosition());
    }
}
