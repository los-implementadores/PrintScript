package org.printscript.interpreter;

import org.printscript.ast.Statement;
import org.printscript.interpreter.builtin.BuiltinFunctions;
import org.printscript.interpreter.scope.Environment;

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
