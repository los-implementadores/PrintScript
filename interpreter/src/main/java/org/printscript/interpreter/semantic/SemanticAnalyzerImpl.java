package org.printscript.interpreter.semantic;

import org.printscript.common.LanguageVersion;
import org.printscript.common.ast.Statement;
import org.printscript.common.env.Environment;
import org.printscript.interpreter.builtin.BuiltinFunctions;

public class SemanticAnalyzerImpl implements SemanticAnalyzer {

  private final SemanticAnalyzerVisitor visitor;

  public SemanticAnalyzerImpl(Environment env, LanguageVersion version) {
    this.visitor = new SemanticAnalyzerVisitor(env, BuiltinFunctions.standard(), version);
  }

  @Override
  public void analyze(Statement statement) {
    statement.accept(visitor);
  }
}
