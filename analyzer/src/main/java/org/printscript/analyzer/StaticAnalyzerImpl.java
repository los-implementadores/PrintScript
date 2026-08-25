package org.printscript.analyzer;

import org.printscript.common.ast.Statement;
import org.printscript.common.configs.AnalyzerConfig;
import org.printscript.common.linterViolations.Violation;

import java.util.List;

public class StaticAnalyzerImpl implements StaticAnalyzer {

    private final AnalyzerVisitor visitor;

    public StaticAnalyzerImpl(AnalyzerConfig config) {
        this.visitor = new AnalyzerVisitor(config);
    }

    @Override
    public void analyze(Statement statement) {
        statement.accept(visitor);
    }

    @Override
    public List<Violation> getViolations() {
        return visitor.getFinalViolations();
    }
}