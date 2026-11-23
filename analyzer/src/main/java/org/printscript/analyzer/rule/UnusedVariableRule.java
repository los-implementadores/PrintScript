package org.printscript.analyzer.rule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.printscript.analyzer.rule.VariableUsage.Declared;
import org.printscript.analyzer.rule.VariableUsage.Usage;
import org.printscript.analyzer.violation.Violation;
import org.printscript.ast.Statement;
import org.printscript.source.Position;

/**
 * Reporta las variables declaradas que nunca se leen ni se reasignan, en la posición de su nombre.
 *
 * <p>Respeta los scopes de bloque: un uso cuenta para la declaración visible más cercana, y el
 * valor de una declaración se lee antes de que la variable exista (como en el semántico). Lo que
 * pasa adentro de cada sentencia lo calcula {@link VariableUsage}. Entre una sentencia y otra solo
 * hay que acordarse de lo que dice {@link Known}.
 */
public final class UnusedVariableRule implements ProgramRule<UnusedVariableRule.Known> {

  @Override
  public Known start() {
    return Known.NOTHING;
  }

  @Override
  public Known after(Known known, Statement statement) {
    return known.after(statement.accept(VariableUsage.INSTANCE));
  }

  @Override
  public List<Violation> violations(Known known) {
    return known.violations();
  }

  /**
   * Lo que la regla sabe del programa hasta una sentencia. Es inmutable: leer una sentencia más
   * devuelve otro.
   *
   * @param unusedOutsideBlocks las variables declaradas afuera de todo bloque que nadie usó
   *     todavía, con la posición de su nombre
   * @param unusedInsideBlocks las variables de bloques que ya terminaron y que nadie usó: eso ya no
   *     puede cambiar
   */
  public record Known(
      Map<String, Position> unusedOutsideBlocks, List<Violation> unusedInsideBlocks) {

    static final Known NOTHING = new Known(Map.of(), List.of());

    public Known {
      unusedOutsideBlocks = Map.copyOf(unusedOutsideBlocks);
      unusedInsideBlocks = List.copyOf(unusedInsideBlocks);
    }

    /** Primero cuenta lo que la sentencia lee y después lo que declara. */
    Known after(Usage usage) {
      boolean nothingNew =
          usage.declares().isEmpty()
              && usage.unused().isEmpty()
              && Collections.disjoint(unusedOutsideBlocks.keySet(), usage.reads());
      if (nothingNew) {
        return this;
      }
      Map<String, Position> outside = new HashMap<>(unusedOutsideBlocks);
      outside.keySet().removeAll(usage.reads());
      usage.declares().ifPresent(declared -> outside.put(declared.name(), declared.position()));
      List<Violation> inside = new ArrayList<>(unusedInsideBlocks);
      inside.addAll(usage.unused());
      return new Known(outside, inside);
    }

    List<Violation> violations() {
      List<Violation> violations = new ArrayList<>(unusedInsideBlocks);
      unusedOutsideBlocks.forEach(
          (name, position) -> violations.add(new Declared(name, position).neverUsed()));
      return violations;
    }
  }
}
