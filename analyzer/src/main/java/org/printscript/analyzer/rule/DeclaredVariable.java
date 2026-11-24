package org.printscript.analyzer.rule;

import org.printscript.analyzer.violation.Violation;
import org.printscript.source.Position;

/**
 * Una variable declarada.
 *
 * @param name su nombre
 * @param position dónde está escrito el nombre
 */
public record DeclaredVariable(String name, Position position) {

  /** El aviso de que nadie la leyó ni la reasignó. */
  Violation neverUsed() {
    return new Violation(
        UnusedVariableRule.KEY, "Variable '" + name + "' is declared but never used.", position);
  }
}
