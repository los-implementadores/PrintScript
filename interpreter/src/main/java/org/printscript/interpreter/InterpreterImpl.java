package org.printscript.interpreter;

import org.printscript.common.LanguageVersion;
import org.printscript.common.ast.Statement;
import org.printscript.common.env.Environment;
import org.printscript.interpreter.env.EnvProvider;
import org.printscript.interpreter.env.SystemEnvProvider;
import org.printscript.interpreter.input.InputProvider;
import org.printscript.interpreter.input.StdinInputProvider;
import org.printscript.interpreter.output.OutputProvider;
import org.printscript.interpreter.output.StdoutOutputProvider;

public class InterpreterImpl implements Interpreter {

  private final InterpreterVisitor visitor;

  public InterpreterImpl(Environment env) {
    this(env, new StdinInputProvider(), new SystemEnvProvider(), LanguageVersion.V1_0);
  }

  public InterpreterImpl(Environment env, InputProvider inputProvider) {
    this(env, inputProvider, new SystemEnvProvider(), LanguageVersion.V1_1);
  }

  public InterpreterImpl(Environment env, InputProvider inputProvider, LanguageVersion version) {
    this(env, inputProvider, new SystemEnvProvider(), version);
  }

  public InterpreterImpl(
      Environment env,
      InputProvider inputProvider,
      EnvProvider envProvider,
      LanguageVersion version) {
    this(env, inputProvider, envProvider, new StdoutOutputProvider(), version);
  }

  public InterpreterImpl(
      Environment env,
      InputProvider inputProvider,
      EnvProvider envProvider,
      OutputProvider outputProvider,
      LanguageVersion version) {
    this.visitor = new InterpreterVisitor(env, inputProvider, envProvider, outputProvider, version);
  }

  @Override
  public void execute(Statement statement) {
    statement.accept(visitor);
  }
}
