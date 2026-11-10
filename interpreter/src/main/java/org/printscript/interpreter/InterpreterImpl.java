package org.printscript.interpreter;

import org.printscript.common.ast.Statement;
import org.printscript.common.env.Environment;
import org.printscript.interpreter.builtin.BuiltinFunctions;

public class InterpreterImpl implements Interpreter {

  private final InterpreterVisitor visitor;

  /**
   * @param builtins las funciones nativas que puede llamar el programa
   */
  public InterpreterImpl(Environment env, ExecutionIo io, BuiltinFunctions builtins) {
    this.visitor = new InterpreterVisitor(env, io, builtins);
  }

  @Override
  public void execute(Statement statement) {
    statement.accept(visitor);
  }
}
