package org.printscript.analyzer;

import org.printscript.common.ast.Statement;
import org.printscript.common.linterViolations.Violation;

import java.util.List;

public interface StaticAnalyzer {
    /**
     * Analiza una sentencia individual y acumula violaciones internamente.
     */
    void analyze(Statement statement);

    /**
     * Devuelve todas las violaciones encontradas tras analizar las sentencias.
     */
    List<Violation> getViolations();
}