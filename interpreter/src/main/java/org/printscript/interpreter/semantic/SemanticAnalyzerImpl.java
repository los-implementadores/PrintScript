package org.printscript.interpreter.semantic;

import org.printscript.common.ast.Statement;
import org.printscript.common.env.Environment;
import org.printscript.interpreter.builtin.BuiltinFunctions;

public class SemanticAnalyzerImpl implements SemanticAnalyzer {

  private final SemanticAnalyzerVisitor visitor;

  /**
   * @param builtins las funciones nativas que puede llamar el programa
   */
  public SemanticAnalyzerImpl(Environment env, BuiltinFunctions builtins) {
    this.visitor = new SemanticAnalyzerVisitor(env, builtins);
  }

  @Override
  public void analyze(Statement statement) {
    statement.accept(visitor);
  }
}
