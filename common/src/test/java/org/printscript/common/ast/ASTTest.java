package org.printscript.common.ast;

import org.junit.jupiter.api.Test;
import org.printscript.common.Position;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests del AST de PrintScript.
 *
 * Cada test construye a mano el árbol que debería producir el parser para
 * un fragmento concreto del lenguaje y verifica su estructura.
 * Sirven como documentación viva de qué árbol se espera para cada construcción.
 */
class ASTTest {

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /** Posición ficticia para no repetirla en cada test. */
    private static final Position POS = new Position(1, 1, 1, 1);

    private Identifier id(String name) {
        return new Identifier(name, POS);
    }

    private NumberLiteral num(double value) {
        return new NumberLiteral(value, POS);
    }

    private StringLiteral str(String value) {
        return new StringLiteral(value, POS);
    }

    // -------------------------------------------------------------------------
    // let name: string = "Joe";
    // -------------------------------------------------------------------------

    @Test
    void varDeclarationConStringLiteral() {
        // let name: string = "Joe";
        VarDeclarationStatement node = new VarDeclarationStatement(
                id("name"), "string", str("Joe"), POS);

        assertEquals("name",  node.getName().getName());
        assertEquals("string", node.getTypeName());
        assertEquals("Joe",   ((StringLiteral) node.getInitializer()).getValue());
    }

    @Test
    void varDeclarationConNumberLiteral() {
        // let a: number = 12;
        VarDeclarationStatement node = new VarDeclarationStatement(
                id("a"), "number", num(12), POS);

        assertEquals("a",      node.getName().getName());
        assertEquals("number", node.getTypeName());
        assertEquals(12.0,     ((NumberLiteral) node.getInitializer()).getValue());
    }

    // -------------------------------------------------------------------------
    // a = a / b;
    // -------------------------------------------------------------------------

    @Test
    void assignmentActualizaVariableExistente() {
        // a = a / b;
        BinaryExpression division = new BinaryExpression(id("a"), "/", id("b"), POS);
        AssignmentStatement node  = new AssignmentStatement(id("a"), division, POS);

        assertEquals("a",   node.getTarget().getName());
        BinaryExpression rhs = (BinaryExpression) node.getValue();
        assertEquals("/",   rhs.getOperator());
        assertEquals("a",   ((Identifier) rhs.getLeft()).getName());
        assertEquals("b",   ((Identifier) rhs.getRight()).getName());
    }

    // -------------------------------------------------------------------------
    // println(name + " " + lastName);
    // -------------------------------------------------------------------------

    @Test
    void printlnConConcatenacionDeStrings() {
        // println(name + " " + lastName)
        // árbol: println( (name + " ") + lastName )
        BinaryExpression innerConcat = new BinaryExpression(id("name"), "+", str(" "), POS);
        BinaryExpression outerConcat = new BinaryExpression(innerConcat, "+", id("lastName"), POS);
        CallExpression   call        = new CallExpression("println", List.of(outerConcat), POS);
        ExpressionStatement stmt     = new ExpressionStatement(call, POS);

        assertEquals("println", call.getCallee());
        assertEquals(1,         call.getArguments().size());
        BinaryExpression arg = (BinaryExpression) call.getArguments().get(0);
        assertEquals("+", arg.getOperator());
        assertEquals("lastName", ((Identifier) arg.getRight()).getName());
    }

    // -------------------------------------------------------------------------
    // println("Result: " + c);   donde c = a / b
    // -------------------------------------------------------------------------

    @Test
    void printlnConConcatenacionStringYNumber() {
        // println("Result: " + c)
        BinaryExpression concat = new BinaryExpression(str("Result: "), "+", id("c"), POS);
        CallExpression   call   = new CallExpression("println", List.of(concat), POS);

        assertEquals("+",         ((BinaryExpression) call.getArguments().get(0)).getOperator());
        assertEquals("Result: ",  ((StringLiteral) ((BinaryExpression) call.getArguments().get(0)).getLeft()).getValue());
        assertEquals("c",         ((Identifier)   ((BinaryExpression) call.getArguments().get(0)).getRight()).getName());
    }

    // -------------------------------------------------------------------------
    // Programa completo: let a: number = 12; let b: number = 4; println("Result: " + a / b);
    // -------------------------------------------------------------------------

    @Test
    void programaCompletoTieneOrdenCorrectoDeSentencias() {
        // let a: number = 12;
        Statement decA = new VarDeclarationStatement(id("a"), "number", num(12), POS);
        // let b: number = 4;
        Statement decB = new VarDeclarationStatement(id("b"), "number", num(4), POS);
        // println("Result: " + a / b);   — simplificado, sin precedencia
        BinaryExpression div    = new BinaryExpression(id("a"), "/", id("b"), POS);
        BinaryExpression concat = new BinaryExpression(str("Result: "), "+", div, POS);
        Statement print = new ExpressionStatement(
                new CallExpression("println", List.of(concat), POS), POS);

        Program program = new Program(List.of(decA, decB, print), POS);

        assertEquals(3, program.getStatements().size());
        assertInstanceOf(VarDeclarationStatement.class, program.getStatements().get(0));
        assertInstanceOf(VarDeclarationStatement.class, program.getStatements().get(1));
        assertInstanceOf(ExpressionStatement.class,     program.getStatements().get(2));
    }

    @Test
    void programaEsInmutable() {
        Program program = new Program(List.of(
                new ExpressionStatement(
                        new CallExpression("println", List.of(num(1)), POS), POS)
        ), POS);

        assertThrows(UnsupportedOperationException.class,
                () -> program.getStatements().add(
                        new ExpressionStatement(num(2), POS)));
    }

    // -------------------------------------------------------------------------
    // Visitor: un pretty-printer mínimo como prueba del patrón
    // -------------------------------------------------------------------------

    @Test
    void visitorRecorreElArbolCorrectamente() {
        // let x: number = 2 + 3;
        BinaryExpression sum  = new BinaryExpression(num(2), "+", num(3), POS);
        VarDeclarationStatement decl = new VarDeclarationStatement(id("x"), "number", sum, POS);
        Program program = new Program(List.of(decl), POS);

        // Visitor que reconstruye el código fuente (simplificado)
        ASTVisitor<String> printer = new ASTVisitor<>() {
            @Override public String visitProgram(Program n) {
                StringBuilder sb = new StringBuilder();
                for (Statement s : n.getStatements()) sb.append(s.accept(this));
                return sb.toString();
            }
            @Override public String visitVarDeclaration(VarDeclarationStatement n) {
                return "let " + n.getName().accept(this)
                        + ": " + n.getTypeName()
                        + " = " + n.getInitializer().accept(this) + ";";
            }
            @Override public String visitAssignment(AssignmentStatement n) {
                return n.getTarget().accept(this) + " = " + n.getValue().accept(this) + ";";
            }
            @Override public String visitExpressionStatement(ExpressionStatement n) {
                return n.getExpression().accept(this) + ";";
            }
            @Override public String visitBinaryExpression(BinaryExpression n) {
                return n.getLeft().accept(this) + " " + n.getOperator() + " " + n.getRight().accept(this);
            }
            @Override public String visitCallExpression(CallExpression n) {
                return n.getCallee() + "(" + n.getArguments().get(0).accept(this) + ")";
            }
            @Override public String visitNumberLiteral(NumberLiteral n) {
                return n.getValue() % 1 == 0
                        ? String.valueOf((long) n.getValue())
                        : String.valueOf(n.getValue());
            }
            @Override public String visitStringLiteral(StringLiteral n) { return "\"" + n.getValue() + "\""; }
            @Override public String visitIdentifier(Identifier n)        { return n.getName(); }
        };

        assertEquals("let x: number = 2 + 3;", program.accept(printer));
    }

    // -------------------------------------------------------------------------
    // Posición se propaga correctamente
    // -------------------------------------------------------------------------

    @Test
    void posicionDeNodoEsAccesible() {
        Position pos = new Position(3, 5, 3, 18);
        NumberLiteral node = new NumberLiteral(42, pos);

        assertEquals(3,  node.getPosition().getStartLine());
        assertEquals(5,  node.getPosition().getStartColumn());
        assertEquals(18, node.getPosition().getEndColumn());
    }
}
