package org.printscript.interpreter;

import org.printscript.common.ast.Statement;
import org.printscript.common.env.Environment;

public class InterpreterImpl implements Interpreter {

  private final InterpreterVisitor visitor;

  public InterpreterImpl(Environment env) {
    this.visitor = new InterpreterVisitor(env);
  }

  @Override
  public void execute(Statement statement) {
    statement.accept(visitor);
  }
}
