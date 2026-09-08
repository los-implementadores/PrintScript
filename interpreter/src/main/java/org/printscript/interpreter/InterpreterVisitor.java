package org.printscript.interpreter;

import java.util.Iterator;
import org.printscript.common.ast.*;
import org.printscript.common.env.Environment;

public class InterpreterVisitor implements ASTVisitor<Object> {

  private final Environment env;
  private final InputProvider inputProvider;
  private final org.printscript.common.LanguageVersion version;
  private String expectedType;

  public InterpreterVisitor(Environment env) {
    this(env, new StdinInputProvider(), org.printscript.common.LanguageVersion.V1_0);
  }

  public InterpreterVisitor(Environment env, InputProvider inputProvider) {
    this(env, inputProvider, org.printscript.common.LanguageVersion.V1_1);
  }

  public InterpreterVisitor(
      Environment env,
      InputProvider inputProvider,
      org.printscript.common.LanguageVersion version) {
    this.env = env;
    this.inputProvider = inputProvider;
    this.version = version;
  }

  @Override
  public Object visitProgram(Program node) {
    Iterator<Statement> it = node.stream();
    while (it.hasNext()) {
      it.next().accept(this);
    }
    return null;
  }

  @Override
  public Object visitVarDeclaration(VarDeclarationStatement node) {
    String name = node.getName().getName();
    String type = node.getTypeName();
    Object value = null;

    if (node.getInitializer() != null) {
      String prev = this.expectedType;
      try {
        this.expectedType = type;
        value = node.getInitializer().accept(this);
      } finally {
        this.expectedType = prev;
      }
    }

    env.define(name, type, value, node.isConst());
    return null;
  }

  @Override
  public Object visitAssignment(AssignmentStatement node) {
    String name = node.getTarget().getName();
    String varType = env.getType(name);
    String prev = this.expectedType;
    Object value;
    try {
      this.expectedType = varType;
      value = node.getValue().accept(this);
    } finally {
      this.expectedType = prev;
    }

    env.assign(name, value);
    return null;
  }

  @Override
  public Object visitExpressionStatement(ExpressionStatement node) {
    node.getExpression().accept(this);
    return null;
  }

  @Override
  public Object visitIf(IfStatement node) {
    Object cond = node.getCondition().accept(this);
    if (!(cond instanceof Boolean b)) {
      throw new RuntimeException(
          "Execution Error at " + node.getPosition() + ": if condition must be boolean.");
    }
    if (b) {
      for (Statement stmt : node.getThenBlock()) {
        stmt.accept(this);
      }
    } else if (node.hasElse()) {
      for (Statement stmt : node.getElseBlock()) {
        stmt.accept(this);
      }
    }
    return null;
  }

  @Override
  public Object visitBinaryExpression(BinaryExpression node) {
    Object left = node.getLeft().accept(this);
    Object right = node.getRight().accept(this);
    String op = node.getOperator();

    if (op.equals("+")) {
      if (left instanceof String || right instanceof String) {
        return formatPrintValue(left) + formatPrintValue(right);
      }
      return ((Double) left) + ((Double) right);
    }

    double l = (Double) left;
    double r = (Double) right;

    return switch (op) {
      case "-" -> l - r;
      case "*" -> l * r;
      case "/" -> {
        if (r == 0) {
          throw new RuntimeException(
              "Execution Error at " + node.getPosition() + ": Division by zero.");
        }
        yield l / r;
      }
      default -> throw new RuntimeException("Unknown operator: " + op);
    };
  }

  @Override
  public Object visitCallExpression(CallExpression node) {
    if (node.getCallee().equals("println")) {
      return executePrintln(node);
    }
    if (node.getCallee().equals("readInput")) {
      return executeReadInput(node);
    }
    throw new RuntimeException(
        "Execution Error at "
            + node.getPosition()
            + ": Unknown function '"
            + node.getCallee()
            + "'");
  }

  private Object executePrintln(CallExpression node) {
    if (node.getArguments().isEmpty()) {
      throw new RuntimeException(
          "Execution Error at " + node.getPosition() + ": println expects 1 argument.");
    }
    String prev = this.expectedType;
    Object val;
    try {
      this.expectedType = "string";
      val = node.getArguments().get(0).accept(this);
    } finally {
      this.expectedType = prev;
    }
    System.out.println(formatPrintValue(val));
    return null;
  }

  private Object executeReadInput(CallExpression node) {
    if (version == org.printscript.common.LanguageVersion.V1_0) {
      throw new RuntimeException(
          "Execution Error at "
              + node.getPosition()
              + ": readInput is only supported in PrintScript 1.1.");
    }
    if (node.getArguments().size() != 1) {
      throw new RuntimeException(
          "Execution Error at " + node.getPosition() + ": readInput expects 1 argument.");
    }

    String prev = this.expectedType;
    Object promptVal;
    try {
      this.expectedType = "string";
      promptVal = node.getArguments().get(0).accept(this);
    } finally {
      this.expectedType = prev;
    }

    String prompt = formatPrintValue(promptVal);
    System.out.println(prompt);

    String raw = inputProvider.readInput(prompt);
    if (raw == null) {
      throw new RuntimeException(
          "Execution Error at "
              + node.getPosition()
              + ": End of input reached while reading input.");
    }

    String targetType = expectedType != null ? expectedType : "string";
    return coerceInput(raw, targetType, node.getPosition());
  }

  private Object coerceInput(String raw, String targetType, org.printscript.common.Position pos) {
    return switch (targetType) {
      case "string" -> raw;
      case "number" -> {
        try {
          yield Double.parseDouble(raw.trim());
        } catch (NumberFormatException e) {
          throw new RuntimeException(
              "Execution Error at " + pos + ": Cannot coerce input '" + raw + "' to number.");
        }
      }
      case "boolean" -> {
        String trimmed = raw.trim();
        if ("true".equalsIgnoreCase(trimmed)) {
          yield Boolean.TRUE;
        } else if ("false".equalsIgnoreCase(trimmed)) {
          yield Boolean.FALSE;
        } else {
          throw new RuntimeException(
              "Execution Error at " + pos + ": Cannot coerce input '" + raw + "' to boolean.");
        }
      }
      default -> raw;
    };
  }

  @Override
  public Object visitNumberLiteral(NumberLiteral node) {
    return node.getValue();
  }

  @Override
  public Object visitStringLiteral(StringLiteral node) {
    return node.getValue();
  }

  @Override
  public Object visitBooleanLiteral(BooleanLiteral node) {
    return node.getValue();
  }

  @Override
  public Object visitIdentifier(Identifier node) {
    return env.get(node.getName());
  }

  private String formatPrintValue(Object val) {
    if (val instanceof Double d && d % 1 == 0) {
      return String.valueOf(d.longValue());
    }
    return String.valueOf(val);
  }
}
