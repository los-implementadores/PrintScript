package org.printscript.common.ast;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Iterator;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.printscript.common.Position;

/**
 * Tests del AST de PrintScript.
 *
 * <p>Cada test construye a mano el árbol que debería producir el parser para un fragmento concreto
 * del lenguaje y verifica su estructura. Sirven como documentación viva de qué árbol se espera para
 * cada construcción.
 */
class ASTTest {

  // -------------------------------------------------------------------------
  // Helpers
  // -------------------------------------------------------------------------

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

  private Program program(Statement... stmts) {
    return new EagerProgram(List.of(stmts), POS);
  }

  // -------------------------------------------------------------------------
  // let name: string = "Joe";
  // -------------------------------------------------------------------------

  @Test
  void varDeclarationConStringLiteral() {
    VarDeclarationStatement node =
        new VarDeclarationStatement(id("name"), "string", str("Joe"), POS);

    assertEquals("name", node.getName().getName());
    assertEquals("string", node.getTypeName());
    assertEquals("Joe", ((StringLiteral) node.getInitializer()).getValue());
  }

  @Test
  void varDeclarationConNumberLiteral() {
    VarDeclarationStatement node = new VarDeclarationStatement(id("a"), "number", num(12), POS);

    assertEquals("a", node.getName().getName());
    assertEquals("number", node.getTypeName());
    assertEquals(12.0, ((NumberLiteral) node.getInitializer()).getValue());
  }

  // -------------------------------------------------------------------------
  // a = a / b;
  // -------------------------------------------------------------------------

  @Test
  void assignmentActualizaVariableExistente() {
    BinaryExpression division = new BinaryExpression(id("a"), "/", id("b"), POS);
    AssignmentStatement node = new AssignmentStatement(id("a"), division, POS);

    assertEquals("a", node.getTarget().getName());
    BinaryExpression rhs = (BinaryExpression) node.getValue();
    assertEquals("/", rhs.getOperator());
    assertEquals("a", ((Identifier) rhs.getLeft()).getName());
    assertEquals("b", ((Identifier) rhs.getRight()).getName());
  }

  // -------------------------------------------------------------------------
  // println(name + " " + lastName);
  // -------------------------------------------------------------------------

  @Test
  void printlnConConcatenacionDeStrings() {
    BinaryExpression innerConcat = new BinaryExpression(id("name"), "+", str(" "), POS);
    BinaryExpression outerConcat = new BinaryExpression(innerConcat, "+", id("lastName"), POS);
    CallExpression call = new CallExpression("println", List.of(outerConcat), POS);

    assertEquals("println", call.getCallee());
    assertEquals(1, call.getArguments().size());
    BinaryExpression arg = (BinaryExpression) call.getArguments().get(0);
    assertEquals("+", arg.getOperator());
    assertEquals("lastName", ((Identifier) arg.getRight()).getName());
  }

  // -------------------------------------------------------------------------
  // Program: toList() y stream()
  // -------------------------------------------------------------------------

  @Test
  void programaCompletoTieneOrdenCorrectoDeSentencias() {
    Statement decA = new VarDeclarationStatement(id("a"), "number", num(12), POS);
    Statement decB = new VarDeclarationStatement(id("b"), "number", num(4), POS);
    Statement print =
        new ExpressionStatement(new CallExpression("println", List.of(id("a")), POS), POS);

    Program program = program(decA, decB, print);

    List<Statement> stmts = program.toList();
    assertEquals(3, stmts.size());
    assertInstanceOf(VarDeclarationStatement.class, stmts.get(0));
    assertInstanceOf(VarDeclarationStatement.class, stmts.get(1));
    assertInstanceOf(ExpressionStatement.class, stmts.get(2));
  }

  @Test
  void toListEsInmutable() {
    Program program =
        program(new ExpressionStatement(new CallExpression("println", List.of(num(1)), POS), POS));

    assertThrows(
        UnsupportedOperationException.class,
        () -> program.toList().add(new ExpressionStatement(num(2), POS)));
  }

  @Test
  void streamProduceTodasLasSentencias() {
    Statement decA = new VarDeclarationStatement(id("a"), "number", num(1), POS);
    Statement decB = new VarDeclarationStatement(id("b"), "number", num(2), POS);
    Program program = program(decA, decB);

    Iterator<Statement> it = program.stream();
    assertTrue(it.hasNext());
    assertInstanceOf(VarDeclarationStatement.class, it.next());
    assertTrue(it.hasNext());
    assertInstanceOf(VarDeclarationStatement.class, it.next());
    assertFalse(it.hasNext());
  }

  @Test
  void lazyProgramToListMaterializaYCachea() {
    Statement stmt = new VarDeclarationStatement(id("x"), "number", num(5), POS);
    Iterator<Statement> source = List.of(stmt).iterator();
    LazyProgram lazy = new LazyProgram(source, POS);

    // primera llamada: drena el iterator
    List<Statement> first = lazy.toList();
    assertEquals(1, first.size());

    // segunda llamada: devuelve el cache (el iterator ya está agotado)
    List<Statement> second = lazy.toList();
    assertSame(first, second);
  }

  @Test
  void lazyProgramStreamEsLazy() {
    Statement stmt1 = new VarDeclarationStatement(id("a"), "number", num(1), POS);
    Statement stmt2 = new VarDeclarationStatement(id("b"), "number", num(2), POS);
    Iterator<Statement> source = List.of(stmt1, stmt2).iterator();
    LazyProgram lazy = new LazyProgram(source, POS);

    Iterator<Statement> it = lazy.stream();
    assertTrue(it.hasNext());
    assertEquals(stmt1, it.next());
    assertTrue(it.hasNext());
    assertEquals(stmt2, it.next());
    assertFalse(it.hasNext());
  }

  // -------------------------------------------------------------------------
  // Visitor: un pretty-printer mínimo como prueba del patrón
  // -------------------------------------------------------------------------

  @Test
  void visitorRecorreElArbolCorrectamente() {
    BinaryExpression sum = new BinaryExpression(num(2), "+", num(3), POS);
    VarDeclarationStatement decl = new VarDeclarationStatement(id("x"), "number", sum, POS);
    Program prog = program(decl);

    ASTVisitor<String> printer =
        new ASTVisitor<>() {
          @Override
          public String visitProgram(Program n) {
            StringBuilder sb = new StringBuilder();
            for (Statement s : n.toList()) sb.append(s.accept(this));
            return sb.toString();
          }

          @Override
          public String visitVarDeclaration(VarDeclarationStatement n) {
            return "let "
                + n.getName().accept(this)
                + ": "
                + n.getTypeName()
                + " = "
                + n.getInitializer().accept(this)
                + ";";
          }

          @Override
          public String visitAssignment(AssignmentStatement n) {
            return n.getTarget().accept(this) + " = " + n.getValue().accept(this) + ";";
          }

          @Override
          public String visitExpressionStatement(ExpressionStatement n) {
            return n.getExpression().accept(this) + ";";
          }

          @Override
          public String visitBinaryExpression(BinaryExpression n) {
            return n.getLeft().accept(this)
                + " "
                + n.getOperator()
                + " "
                + n.getRight().accept(this);
          }

          @Override
          public String visitCallExpression(CallExpression n) {
            return n.getCallee() + "(" + n.getArguments().get(0).accept(this) + ")";
          }

          @Override
          public String visitNumberLiteral(NumberLiteral n) {
            return n.getValue() % 1 == 0
                ? String.valueOf((long) n.getValue())
                : String.valueOf(n.getValue());
          }

          @Override
          public String visitStringLiteral(StringLiteral n) {
            return "\"" + n.getValue() + "\"";
          }

          @Override
          public String visitIdentifier(Identifier n) {
            return n.getName();
          }

          @Override
          public String visitBooleanLiteral(BooleanLiteral n) {
            return String.valueOf(n.getValue());
          }
        };

    assertEquals("let x: number = 2 + 3;", prog.accept(printer));
  }

  // -------------------------------------------------------------------------
  // Posición
  // -------------------------------------------------------------------------

  @Test
  void posicionDeNodoEsAccesible() {
    Position pos = new Position(3, 5, 3, 18);
    NumberLiteral node = new NumberLiteral(42, pos);

    assertEquals(3, node.getPosition().getStartLine());
    assertEquals(5, node.getPosition().getStartColumn());
    assertEquals(18, node.getPosition().getEndColumn());
  }
}
