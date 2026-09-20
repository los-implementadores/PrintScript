package org.printscript.interpreter.semantic;

import java.util.Iterator;
import org.printscript.common.ast.*;
import org.printscript.common.env.Environment;

public class SemanticAnalyzerVisitor implements ASTVisitor<String> {

  private final Environment env;
  private final org.printscript.common.LanguageVersion version;
  private String expectedType;

  public SemanticAnalyzerVisitor(Environment env) {
    this(env, org.printscript.common.LanguageVersion.V1_0);
  }

  public SemanticAnalyzerVisitor(Environment env, org.printscript.common.LanguageVersion version) {
    this.env = env;
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

    if (env.isDeclared(varName)) {
      throw new RuntimeException(
          "Semantic Error at "
              + node.getPosition()
              + ": Variable '"
              + varName
              + "' is already declared.");
    }

    if (node.getInitializer() != null) {
      String prev = this.expectedType;
      String exprType;
      try {
        this.expectedType = declaredType;
        exprType = node.getInitializer().accept(this);
      } finally {
        this.expectedType = prev;
      }
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
    String prev = this.expectedType;
    String exprType;
    try {
      this.expectedType = varType;
      exprType = node.getValue().accept(this);
    } finally {
      this.expectedType = prev;
    }

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
  public String visitIf(IfStatement node) {
    String condType = node.getCondition().accept(this);
    if (!"boolean".equals(condType)) {
      throw new RuntimeException(
          "Semantic Error at "
              + node.getPosition()
              + ": if condition must be boolean, found "
              + condType
              + ".");
    }
    for (Statement stmt : node.getThenBlock()) {
      stmt.accept(this);
    }
    if (node.hasElse()) {
      for (Statement stmt : node.getElseBlock()) {
        stmt.accept(this);
      }
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
    if (node.getCallee().equals("println")) {
      return analyzePrintln(node);
    }
    if (node.getCallee().equals("readInput")) {
      return analyzeReadInput(node);
    }
    if (node.getCallee().equals("readEnv")) {
      return analyzeReadEnv(node);
    }
    throw new RuntimeException(
        "Semantic Error at "
            + node.getPosition()
            + ": Unknown function '"
            + node.getCallee()
            + "'");
  }

  private String analyzePrintln(CallExpression node) {
    if (node.getArguments().size() != 1) {
      throw new RuntimeException(
          "Semantic Error at " + node.getPosition() + ": println expects 1 argument.");
    }
    String prev = this.expectedType;
    try {
      this.expectedType = "string";
      node.getArguments().get(0).accept(this);
    } finally {
      this.expectedType = prev;
    }
    return null;
  }

  private String analyzeReadInput(CallExpression node) {
    if (version == org.printscript.common.LanguageVersion.V1_0) {
      throw new RuntimeException(
          "Semantic Error at "
              + node.getPosition()
              + ": Function 'readInput' is only supported in PrintScript 1.1.");
    }
    if (node.getArguments().size() != 1) {
      throw new RuntimeException(
          "Semantic Error at " + node.getPosition() + ": readInput expects 1 argument.");
    }

    String prev = this.expectedType;
    String argType;
    try {
      this.expectedType = "string";
      argType = node.getArguments().get(0).accept(this);
    } finally {
      this.expectedType = prev;
    }

    if (!"string".equals(argType)) {
      throw new RuntimeException(
          "Semantic Error at "
              + node.getPosition()
              + ": readInput argument must be of type string, found "
              + argType);
    }

    return expectedType != null ? expectedType : "string";
  }

  private String analyzeReadEnv(CallExpression node) {
    if (version == org.printscript.common.LanguageVersion.V1_0) {
      throw new RuntimeException(
          "Semantic Error at "
              + node.getPosition()
              + ": Function 'readEnv' is only supported in PrintScript 1.1.");
    }
    if (node.getArguments().size() != 1) {
      throw new RuntimeException(
          "Semantic Error at " + node.getPosition() + ": readEnv expects 1 argument.");
    }

    String prev = this.expectedType;
    String argType;
    try {
      this.expectedType = "string";
      argType = node.getArguments().get(0).accept(this);
    } finally {
      this.expectedType = prev;
    }

    if (!"string".equals(argType)) {
      throw new RuntimeException(
          "Semantic Error at "
              + node.getPosition()
              + ": readEnv argument must be of type string, found "
              + argType);
    }

    return expectedType != null ? expectedType : "string";
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
