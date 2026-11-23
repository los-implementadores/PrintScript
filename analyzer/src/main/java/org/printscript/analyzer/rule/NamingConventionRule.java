package org.printscript.analyzer.rule;

import java.util.List;
import java.util.Objects;
import org.printscript.analyzer.violation.Violation;
import org.printscript.ast.Identifier;
import org.printscript.ast.VarDeclarationStatement;

/**
 * Exige que los nombres de variables sigan una convención (camelCase o snake_case). Marca el
 * nombre, no la declaración entera.
 */
public final class NamingConventionRule implements NodeRule<VarDeclarationStatement> {

  private final NamingConvention convention;

  public NamingConventionRule(NamingConvention convention) {
    this.convention =
        Objects.requireNonNull(
            convention, "La regla de nombres necesita una convención (camelCase o snake_case)");
  }

  @Override
  public Class<VarDeclarationStatement> nodeType() {
    return VarDeclarationStatement.class;
  }

  @Override
  public List<Violation> check(VarDeclarationStatement declaration) {
    Identifier name = declaration.getName();
    if (convention.matches(name.getName())) {
      return List.of();
    }
    return List.of(
        new Violation(
            "Variable '" + name.getName() + "' should be in " + convention.displayName() + ".",
            name.getPosition()));
  }
}
