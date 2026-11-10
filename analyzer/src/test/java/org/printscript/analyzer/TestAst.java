package org.printscript.analyzer;

import java.util.List;
import org.printscript.ast.BinaryExpression;
import org.printscript.ast.Block;
import org.printscript.ast.BooleanLiteral;
import org.printscript.ast.CallExpression;
import org.printscript.ast.Expression;
import org.printscript.ast.ExpressionStatement;
import org.printscript.ast.Identifier;
import org.printscript.ast.IfStatement;
import org.printscript.ast.NumberLiteral;
import org.printscript.ast.Statement;
import org.printscript.ast.VarDeclarationStatement;
import org.printscript.source.Position;

/** Arma a mano los nodos que usan los tests del linter. */
final class TestAst {

  static final Position POS = new Position(1, 1, 1, 10);

  private TestAst() {}

  static VarDeclarationStatement declaration(String name, Position position) {
    return new VarDeclarationStatement(
        new Identifier(name, position), "number", new NumberLiteral(1, position), position);
  }

  static Statement call(String function, Expression argument) {
    return new ExpressionStatement(new CallExpression(function, List.of(argument), POS), POS);
  }

  static Statement println(String name) {
    return call("println", new Identifier(name, POS));
  }

  static IfStatement ifElse(List<Statement> thenBlock, List<Statement> elseBlock) {
    return new IfStatement(
        new BooleanLiteral(true, POS),
        new Block(thenBlock, POS),
        elseBlock == null ? null : new Block(elseBlock, POS),
        POS);
  }

  static BinaryExpression sum() {
    return new BinaryExpression(new NumberLiteral(2, POS), "+", new NumberLiteral(2, POS), POS);
  }
}
