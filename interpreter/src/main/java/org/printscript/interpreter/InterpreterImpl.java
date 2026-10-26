package org.printscript.interpreter;

import org.printscript.common.LanguageVersion;
import org.printscript.common.ast.Statement;
import org.printscript.common.env.Environment;
import org.printscript.interpreter.builtin.BuiltinFunctions;
import org.printscript.interpreter.env.EnvProvider;
import org.printscript.interpreter.input.InputProvider;
import org.printscript.interpreter.output.StdoutOutputProvider;

public class InterpreterImpl implements Interpreter {

  private final InterpreterVisitor visitor;

  public InterpreterImpl(Environment env, ExecutionIo io, LanguageVersion version) {
    this.visitor = new InterpreterVisitor(env, io, BuiltinFunctions.standard(), version);
  }

  /**
   * Constructor que usa el adapter del TCK, que captura {@code println} reemplazando {@code
   * System.out}: por eso imprime en stdout.
   *
   * @deprecated sólo se mantiene para no romper el adapter del TCK. Usar {@link
   *     #InterpreterImpl(Environment, ExecutionIo, LanguageVersion)} e inyectar el {@code
   *     OutputProvider}.
   */
  @Deprecated
  public InterpreterImpl(
      Environment env,
      InputProvider inputProvider,
      EnvProvider envProvider,
      LanguageVersion version) {
    this(env, new ExecutionIo(inputProvider, envProvider, new StdoutOutputProvider()), version);
  }

  @Override
  public void execute(Statement statement) {
    statement.accept(visitor);
  }
}
