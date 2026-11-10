package org.printscript.interpreter;

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
import org.printscript.interpreter.builtin.CallContext;
import org.printscript.interpreter.scope.Environment;
import org.printscript.interpreter.util.ValueFormatter;

public class InterpreterVisitor implements ASTVisitor<Object> {

  /** Scope actual: cambia al entrar y al salir de cada bloque. */
  private Environment env;

  private final ExecutionIo io;
  private final BuiltinFunctions builtins;
  private final CallContext callContext = new VisitorCallContext();
  private String expectedType;

  public InterpreterVisitor(Environment env, ExecutionIo io, BuiltinFunctions builtins) {
    this.env = env;
    this.io = io;
    this.builtins = builtins;
  }

  @Override
  public Object visitVarDeclaration(VarDeclarationStatement node) {
    String type = node.getTypeName();
    Object value = null;
    if (node.getInitializer() != null) {
      value = evaluate(node.getInitializer(), type);
    }
    env.define(node.getName().getName(), type, value, node.isConst());
    return null;
  }

  @Override
  public Object visitAssignment(AssignmentStatement node) {
    String name = node.getTarget().getName();
    Object value = evaluate(node.getValue(), env.getType(name));
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
      throw new ExecutionException("if condition must be boolean.", node.getPosition());
    }
    if (b) {
      node.getThenBlock().accept(this);
    } else if (node.hasElse()) {
      node.getElseBlock().accept(this);
    }
    return null;
  }

  /** Ejecuta un bloque en su propio scope: lo que declara deja de existir al salir. */
  @Override
  public Object visitBlock(Block node) {
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
  public Object visitBinaryExpression(BinaryExpression node) {
    Object left = node.getLeft().accept(this);
    Object right = node.getRight().accept(this);
    String op = node.getOperator();

    if (op.equals("+")) {
      if (left instanceof String || right instanceof String) {
        return ValueFormatter.display(left) + ValueFormatter.display(right);
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
          throw new ExecutionException("Division by zero.", node.getPosition());
        }
        yield l / r;
      }
      default -> throw new ExecutionException("Unknown operator: " + op, node.getPosition());
    };
  }

  @Override
  public Object visitCallExpression(CallExpression node) {
    BuiltinFunction function =
        builtins.resolve(node, detail -> new ExecutionException(detail, node.getPosition()));
    return function.call(callContext, node);
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

  /** Evalúa una expresión con {@code type} como tipo esperado (lo usan readInput y readEnv). */
  private Object evaluate(Expression expression, String type) {
    String previous = this.expectedType;
    try {
      this.expectedType = type;
      return expression.accept(this);
    } finally {
      this.expectedType = previous;
    }
  }

  private final class VisitorCallContext implements CallContext {

    @Override
    public Object evaluate(Expression expression, String type) {
      return InterpreterVisitor.this.evaluate(expression, type);
    }

    @Override
    public String expectedType() {
      return expectedType;
    }

    @Override
    public ExecutionIo io() {
      return io;
    }
  }
}
