package org.printscript.interpreter;

import org.printscript.common.ast.Statement;
import org.printscript.common.env.Environment;

public class SemanticAnalyzerImpl implements SemanticAnalyzer {

    private final SemanticAnalyzerVisitor visitor;

    public SemanticAnalyzerImpl(Environment env) {
        this.visitor = new SemanticAnalyzerVisitor(env);
    }

    @Override
    public void analyze(Statement statement) {
        statement.accept(visitor);
    }
}