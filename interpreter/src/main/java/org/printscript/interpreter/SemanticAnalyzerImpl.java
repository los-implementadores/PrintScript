package org.printscript.interpreter;

import org.printscript.common.LanguageVersion;
import org.printscript.common.ast.Statement;
import org.printscript.common.env.Environment;

public class SemanticAnalyzerImpl implements SemanticAnalyzer {

  private final SemanticAnalyzerVisitor visitor;

  public SemanticAnalyzerImpl(Environment env) {
    this(env, LanguageVersion.V1_0);
  }

  public SemanticAnalyzerImpl(Environment env, LanguageVersion version) {
    this.visitor = new SemanticAnalyzerVisitor(env, version);
  }

  @Override
  public void analyze(Statement statement) {
    statement.accept(visitor);
  }
}
