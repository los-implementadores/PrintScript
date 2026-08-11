package org.printscript.parser;

import org.junit.jupiter.api.Test;
import org.printscript.common.ast.*;
import org.printscript.lexer.LexerImpl;

import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ParserTest {

    private Program parse(String source) {
        LexerImpl lexer = new LexerImpl(new StringReader(source));
        Parser parser = new ParserImpl(lexer);
        return parser.parse();
    }

    // ---------------------------------------------------------------- VarDeclaration

    @Test
    void parsesNumberDeclaration() {
        Program program = parse("let x: number = 42;");

        assertEquals(1, program.getStatements().size());
        VarDeclarationStatement stmt = (VarDeclarationStatement) program.getStatements().get(0);

        assertEquals("x", stmt.getName().getName());
        assertEquals("number", stmt.getTypeName());
        NumberLiteral init = (NumberLiteral) stmt.getInitializer();
        assertEquals(42.0, init.getValue());
    }

    @Test
    void parsesStringDeclaration() {
        Program program = parse("let name: string = \"Joe\";");

        VarDeclarationStatement stmt = (VarDeclarationStatement) program.getStatements().get(0);
        assertEquals("name", stmt.getName().getName());
        assertEquals("string", stmt.getTypeName());
        StringLiteral init = (StringLiteral) stmt.getInitializer();
        assertEquals("Joe", init.getValue());
    }

    // ---------------------------------------------------------------- Assignment

    @Test
    void parsesAssignment() {
        Program program = parse("let x: number = 1;\nx = 99;");

        assertEquals(2, program.getStatements().size());
        AssignmentStatement assign = (AssignmentStatement) program.getStatements().get(1);

        assertEquals("x", assign.getTarget().getName());
        NumberLiteral value = (NumberLiteral) assign.getValue();
        assertEquals(99.0, value.getValue());
    }

    // ---------------------------------------------------------------- ExpressionStatement / println

    @Test
    void parsesPrintlnWithIdentifier() {
        Program program = parse("println(x);");

        ExpressionStatement stmt = (ExpressionStatement) program.getStatements().get(0);
        CallExpression call = (CallExpression) stmt.getExpression();

        assertEquals("println", call.getCallee());
        assertEquals(1, call.getArguments().size());
        Identifier arg = (Identifier) call.getArguments().get(0);
        assertEquals("x", arg.getName());
    }

    @Test
    void parsesPrintlnWithStringLiteral() {
        Program program = parse("println(\"hello\");");

        ExpressionStatement stmt = (ExpressionStatement) program.getStatements().get(0);
        CallExpression call = (CallExpression) stmt.getExpression();
        StringLiteral arg = (StringLiteral) call.getArguments().get(0);
        assertEquals("hello", arg.getValue());
    }

    // ---------------------------------------------------------------- Pratt: precedencia

    @Test
    void respectsMultiplicationOverAddition() {
        // 2 + 3 * 4 debe parsear como 2 + (3 * 4)
        Program program = parse("let r: number = 2 + 3 * 4;");

        VarDeclarationStatement stmt = (VarDeclarationStatement) program.getStatements().get(0);
        BinaryExpression top = (BinaryExpression) stmt.getInitializer();

        assertEquals("+", top.getOperator());
        NumberLiteral left = (NumberLiteral) top.getLeft();
        assertEquals(2.0, left.getValue());

        BinaryExpression right = (BinaryExpression) top.getRight();
        assertEquals("*", right.getOperator());
        assertEquals(3.0, ((NumberLiteral) right.getLeft()).getValue());
        assertEquals(4.0, ((NumberLiteral) right.getRight()).getValue());
    }

    @Test
    void respectsDivisionOverSubtraction() {
        // 10 - 6 / 2 debe parsear como 10 - (6 / 2)
        Program program = parse("let r: number = 10 - 6 / 2;");

        VarDeclarationStatement stmt = (VarDeclarationStatement) program.getStatements().get(0);
        BinaryExpression top = (BinaryExpression) stmt.getInitializer();

        assertEquals("-", top.getOperator());
        BinaryExpression right = (BinaryExpression) top.getRight();
        assertEquals("/", right.getOperator());
    }

    @Test
    void leftAssociativityForAddition() {
        // 1 + 2 + 3 debe parsear como (1 + 2) + 3
        Program program = parse("let r: number = 1 + 2 + 3;");

        VarDeclarationStatement stmt = (VarDeclarationStatement) program.getStatements().get(0);
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
        Program program = parse("let r: number = (2 + 3) * 4;");

        VarDeclarationStatement stmt = (VarDeclarationStatement) program.getStatements().get(0);
        BinaryExpression top = (BinaryExpression) stmt.getInitializer();

        assertEquals("*", top.getOperator());
        BinaryExpression left = (BinaryExpression) top.getLeft();
        assertEquals("+", left.getOperator());
    }

    // ---------------------------------------------------------------- Programa completo

    @Test
    void parsesFullProgram() {
        String source =
                "let name: string = \"Joe\";\n" +
                "let lastName: string = \"Doe\";\n" +
                "println(name + \" \" + lastName);";

        Program program = parse(source);
        assertEquals(3, program.getStatements().size());

        assertTrue(program.getStatements().get(0) instanceof VarDeclarationStatement);
        assertTrue(program.getStatements().get(1) instanceof VarDeclarationStatement);
        assertTrue(program.getStatements().get(2) instanceof ExpressionStatement);
    }

    @Test
    void parsesNumberProgramWithDivision() {
        String source =
                "let a: number = 12;\n" +
                "let b: number = 4;\n" +
                "let c: number = a / b;\n" +
                "println(c);";

        Program program = parse(source);
        assertEquals(4, program.getStatements().size());

        VarDeclarationStatement cDecl = (VarDeclarationStatement) program.getStatements().get(2);
        BinaryExpression div = (BinaryExpression) cDecl.getInitializer();
        assertEquals("/", div.getOperator());
        assertEquals("a", ((Identifier) div.getLeft()).getName());
        assertEquals("b", ((Identifier) div.getRight()).getName());
    }

    // ---------------------------------------------------------------- Posiciones

    @Test
    void tracksPositionOfVarDeclaration() {
        Program program = parse("let x: number = 5;");

        VarDeclarationStatement stmt = (VarDeclarationStatement) program.getStatements().get(0);
        assertEquals(1, stmt.getPosition().getStartLine());
        assertEquals(1, stmt.getPosition().getStartColumn());
    }

    // ---------------------------------------------------------------- Errores

    @Test
    void throwsOnMissingColon() {
        assertThrows(ParseException.class, () -> parse("let x number = 5;"));
    }

    @Test
    void throwsOnMissingSemicolon() {
        assertThrows(ParseException.class, () -> parse("let x: number = 5"));
    }

    @Test
    void throwsOnUnknownType() {
        assertThrows(ParseException.class, () -> parse("let x: boolean = true;"));
    }

    @Test
    void throwsOnUnexpectedToken() {
        assertThrows(ParseException.class, () -> parse("let x: number = ;"));
    }

    @Test
    void parseExceptionContainsPosition() {
        ParseException ex = assertThrows(ParseException.class, () -> parse("let x: number = ;"));
        assertNotNull(ex.getPosition());
    }
}
