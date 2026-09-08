package org.printscript.interpreter;

import java.util.Iterator;
import org.printscript.common.ast.*;
import org.printscript.common.env.Environment;

public class SemanticAnalyzerVisitor implements ASTVisitor<String> {

  private final Environment env;

  public SemanticAnalyzerVisitor(Environment env) {
    this.env = env;
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

    if (env.isDeclared(varName)) {
      throw new RuntimeException(
          "Semantic Error at "
              + node.getPosition()
              + ": Variable '"
              + varName
              + "' is already declared.");
    }

    if (node.getInitializer() != null) {
      String exprType = node.getInitializer().accept(this);
      if (!declaredType.equals(exprType)) {
        throw new RuntimeException(
            "Semantic Error at "
                + node.getPosition()
                + ": Cannot assign "
                + exprType
                + " to variable of type "
                + declaredType);
      }
    }

    env.define(varName, declaredType, null, node.isConst());
    return null;
  }

  @Override
  public String visitAssignment(AssignmentStatement node) {
    String varName = node.getTarget().getName();

    if (!env.isDeclared(varName)) {
      throw new RuntimeException(
          "Semantic Error at "
              + node.getPosition()
              + ": Variable '"
              + varName
              + "' is not declared.");
    }

    if (env.isConst(varName)) {
      throw new RuntimeException(
          "Semantic Error at "
              + node.getPosition()
              + ": Cannot reassign constant '"
              + varName
              + "'.");
    }

    String varType = env.getType(varName);
    String exprType = node.getValue().accept(this);

    if (!varType.equals(exprType)) {
      throw new RuntimeException(
          "Semantic Error at "
              + node.getPosition()
              + ": Cannot assign "
              + exprType
              + " to variable of type "
              + varType);
    }

    return null;
  }

  @Override
  public String visitExpressionStatement(ExpressionStatement node) {
    node.getExpression().accept(this);
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
        throw new RuntimeException(
            "Semantic Error at "
                + node.getPosition()
                + ": Operator '+' requires numeric or string operands.");
      }
      return "number";
    }

    if (op.equals("-") || op.equals("*") || op.equals("/")) {
      if (!leftType.equals("number") || !rightType.equals("number")) {
        throw new RuntimeException(
            "Semantic Error at "
                + node.getPosition()
                + ": Operator '"
                + op
                + "' requires numeric operands.");
      }
      return "number";
    }

    throw new RuntimeException(
        "Semantic Error at " + node.getPosition() + ": Unknown operator " + op);
  }

  @Override
  public String visitCallExpression(CallExpression node) {
    if (!node.getCallee().equals("println")) {
      throw new RuntimeException(
          "Semantic Error at "
              + node.getPosition()
              + ": Unknown function '"
              + node.getCallee()
              + "'");
    }

    if (node.getArguments().size() != 1) {
      throw new RuntimeException(
          "Semantic Error at " + node.getPosition() + ": println expects 1 argument.");
    }

    node.getArguments().get(0).accept(this);
    return null;
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
      throw new RuntimeException(
          "Semantic Error at "
              + node.getPosition()
              + ": Variable '"
              + node.getName()
              + "' is not declared.");
    }
    return env.getType(node.getName());
  }
}
