package org.printscript.interpreter.semantic;

import org.printscript.ast.ASTVisitor;
import org.printscript.ast.AssignmentStatement;
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
import org.printscript.ast.StringLiteral;
import org.printscript.ast.VarDeclarationStatement;
import org.printscript.interpreter.builtin.BuiltinFunction;
import org.printscript.interpreter.builtin.BuiltinFunctions;
import org.printscript.interpreter.builtin.TypeChecker;
import org.printscript.interpreter.scope.Environment;
import org.printscript.source.Position;

public class SemanticAnalyzerVisitor implements ASTVisitor<String> {

  /** Scope actual: cambia al entrar y al salir de cada bloque, igual que en el intérprete. */
  private Environment env;

  private final BuiltinFunctions builtins;
  private final TypeChecker typeChecker = new VisitorTypeChecker();
  private String expectedType;

  public SemanticAnalyzerVisitor(Environment env, BuiltinFunctions builtins) {
    this.env = env;
    this.builtins = builtins;
  }

  @Override
  public String visitVarDeclaration(VarDeclarationStatement node) {
    String varName = node.getName().getName();
    String declaredType = node.getTypeName();

    if (env.isDeclaredInThisScope(varName)) {
      throw new SemanticException(
          "Variable '" + varName + "' is already declared.", node.getPosition());
    }

    if (node.getInitializer() != null) {
      String exprType = typeOf(node.getInitializer(), declaredType);
      if (!declaredType.equals(exprType)) {
        throw new SemanticException(
            "Cannot assign " + exprType + " to variable of type " + declaredType,
            node.getPosition());
      }
    }

    env.define(varName, declaredType, null, node.isConst());
    return null;
  }

  @Override
  public String visitAssignment(AssignmentStatement node) {
    String varName = node.getTarget().getName();

    if (!env.isDeclared(varName)) {
      throw new SemanticException(
          "Variable '" + varName + "' is not declared.", node.getPosition());
    }

    if (env.isConst(varName)) {
      throw new SemanticException(
          "Cannot reassign constant '" + varName + "'.", node.getPosition());
    }

    String varType = env.getType(varName);
    String exprType = typeOf(node.getValue(), varType);
    if (!varType.equals(exprType)) {
      throw new SemanticException(
          "Cannot assign " + exprType + " to variable of type " + varType, node.getPosition());
    }

    return null;
  }

  @Override
  public String visitExpressionStatement(ExpressionStatement node) {
    node.getExpression().accept(this);
    return null;
  }

  @Override
  public String visitIf(IfStatement node) {
    String condType = node.getCondition().accept(this);
    if (!"boolean".equals(condType)) {
      throw new SemanticException(
          "if condition must be boolean, found " + condType + ".", node.getPosition());
    }
    // Cada rama en su propio scope: lo que se declara en una no existe en la otra ni después del
    // if.
    node.getThenBlock().accept(this);
    if (node.hasElse()) {
      node.getElseBlock().accept(this);
    }
    return null;
  }

  /** Analiza un bloque en su propio scope, igual que lo ejecuta el intérprete. */
  @Override
  public String visitBlock(Block node) {
    Environment outer = env;
    env = outer.childScope();
    try {
      for (Statement stmt : node.getStatements()) {
        stmt.accept(this);
      }
    } finally {
      env = outer;
    }
    return null;
  }

  @Override
  public String visitBinaryExpression(BinaryExpression node) {
    String leftType = node.getLeft().accept(this);
    String rightType = node.getRight().accept(this);
    String op = node.getOperator();

    if (op.equals("+")) {
      if (leftType.equals("string") || rightType.equals("string")) {
        return "string";
      }
      if (!leftType.equals("number") || !rightType.equals("number")) {
        throw new SemanticException(
            "Operator '+' requires numeric or string operands.", node.getPosition());
      }
      return "number";
    }

    if (op.equals("-") || op.equals("*") || op.equals("/")) {
      if (!leftType.equals("number") || !rightType.equals("number")) {
        throw new SemanticException(
            "Operator '" + op + "' requires numeric operands.", node.getPosition());
      }
      return "number";
    }

    throw new SemanticException("Unknown operator " + op, node.getPosition());
  }

  @Override
  public String visitCallExpression(CallExpression node) {
    BuiltinFunction function =
        builtins.resolve(node, detail -> new SemanticException(detail, node.getPosition()));
    return function.checkTypes(typeChecker, node);
  }

  @Override
  public String visitNumberLiteral(NumberLiteral node) {
    return "number";
  }

  @Override
  public String visitStringLiteral(StringLiteral node) {
    return "string";
  }

  @Override
  public String visitBooleanLiteral(BooleanLiteral node) {
    return "boolean";
  }

  @Override
  public String visitIdentifier(Identifier node) {
    if (!env.isDeclared(node.getName())) {
      throw new SemanticException(
          "Variable '" + node.getName() + "' is not declared.", node.getPosition());
    }
    return env.getType(node.getName());
  }

  /** Tipo de una expresión analizada con {@code type} como tipo esperado. */
  private String typeOf(Expression expression, String type) {
    String previous = this.expectedType;
    try {
      this.expectedType = type;
      return expression.accept(this);
    } finally {
      this.expectedType = previous;
    }
  }

  private final class VisitorTypeChecker implements TypeChecker {

    @Override
    public String typeOf(Expression expression, String type) {
      return SemanticAnalyzerVisitor.this.typeOf(expression, type);
    }

    @Override
    public String expectedType() {
      return expectedType;
    }

    @Override
    public RuntimeException error(String detail, Position position) {
      return new SemanticException(detail, position);
    }
  }
}
