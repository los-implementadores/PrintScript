package org.printscript.interpreter.semantic;

import java.util.Iterator;
import java.util.List;
import org.printscript.common.LanguageVersion;
import org.printscript.common.ast.*;
import org.printscript.common.env.Environment;
import org.printscript.interpreter.builtin.BuiltinFunction;
import org.printscript.interpreter.builtin.BuiltinFunctions;
import org.printscript.interpreter.builtin.TypeChecker;

public class SemanticAnalyzerVisitor implements ASTVisitor<String> {

  /** Scope actual: cambia al entrar y al salir de cada bloque, igual que en el intérprete. */
  private Environment env;

  private final BuiltinFunctions builtins;
  private final LanguageVersion version;
  private final TypeChecker typeChecker = new VisitorTypeChecker();
  private String expectedType;

  public SemanticAnalyzerVisitor(
      Environment env, BuiltinFunctions builtins, LanguageVersion version) {
    this.env = env;
    this.builtins = builtins;
    this.version = version;
  }

  @Override
  public String visitProgram(Program node) {
    Iterator<Statement> it = node.stream();
    while (it.hasNext()) {
      it.next().accept(this);
    }
    return null;
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
    analyzeBlock(node.getThenBlock());
    if (node.hasElse()) {
      analyzeBlock(node.getElseBlock());
    }
    return null;
  }

  private void analyzeBlock(List<Statement> block) {
    Environment outer = env;
    env = outer.childScope();
    try {
      for (Statement stmt : block) {
        stmt.accept(this);
      }
    } finally {
      env = outer;
    }
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
        builtins.resolve(
            node, version, detail -> new SemanticException(detail, node.getPosition()));
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
  }
}
