package org.printscript.interpreter;

import org.printscript.common.LanguageVersion;
import org.printscript.common.ast.Statement;
import org.printscript.common.env.Environment;

public class InterpreterImpl implements Interpreter {

  private final InterpreterVisitor visitor;

  public InterpreterImpl(Environment env) {
    this(env, new StdinInputProvider(), LanguageVersion.V1_0);
  }

  public InterpreterImpl(Environment env, InputProvider inputProvider) {
    this(env, inputProvider, LanguageVersion.V1_1);
  }

  public InterpreterImpl(Environment env, InputProvider inputProvider, LanguageVersion version) {
    this.visitor = new InterpreterVisitor(env, inputProvider, version);
  }

  @Override
  public void execute(Statement statement) {
    statement.accept(visitor);
  }
}
