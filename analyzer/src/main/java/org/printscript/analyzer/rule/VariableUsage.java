package org.printscript.analyzer.rule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.printscript.analyzer.violation.Violation;

/**
 * Lo que hace un pedazo de código con las variables: cuáles declara, cuáles de esas se usan más
 * adelante, cuáles lee de afuera y qué variables sin usar dejaron sus bloques.
 *
 * <p>Es un valor: nunca cambia. Dos pedazos seguidos se juntan con {@link #then}, que es asociativo
 * y tiene a {@link #NOTHING} como neutro. Por eso un bloque, o un programa entero, se arma juntando
 * sus sentencias con {@code then}, una detrás de otra.
 *
 * @param declared las variables declaradas en este pedazo, por nombre
 * @param used los nombres de esas variables que se leen o se reasignan más adelante en el mismo
 *     pedazo
 * @param readsFromOutside los nombres que se leen o se reasignan sin haberlos declarado antes en el
 *     mismo pedazo: son de un scope de afuera
 * @param unusedInClosedBlocks las variables sin usar de los bloques que empiezan y terminan adentro
 *     de este pedazo
 */
public record VariableUsage(
    Map<String, DeclaredVariable> declared,
    Set<String> used,
    Set<String> readsFromOutside,
    List<Violation> unusedInClosedBlocks) {

  /**
   * Un pedazo que no hace nada con las variables, como un literal. Es el neutro de {@link #then}.
   */
  public static final VariableUsage NOTHING =
      new VariableUsage(Map.of(), Set.of(), Set.of(), List.of());

  public VariableUsage {
    declared = Map.copyOf(declared);
    used = Set.copyOf(used);
    readsFromOutside = Set.copyOf(readsFromOutside);
    unusedInClosedBlocks = List.copyOf(unusedInClosedBlocks);
  }

  /** Un pedazo que solo lee (o reasigna) la variable {@code name}. */
  static VariableUsage reading(String name) {
    return new VariableUsage(Map.of(), Set.of(), Set.of(name), List.of());
  }

  /** Un pedazo que solo declara {@code variable}. */
  static VariableUsage declaring(DeclaredVariable variable) {
    return new VariableUsage(Map.of(variable.name(), variable), Set.of(), Set.of(), List.of());
  }

  /**
   * Este pedazo y, después, {@code next}. Lo que {@code next} lee de afuera y se declaró en este
   * pedazo pasa a ser un uso de esa variable; lo demás sigue siendo de afuera. No cambia ninguno de
   * los dos: devuelve uno nuevo.
   */
  public VariableUsage then(VariableUsage next) {
    Set<String> usedNow = new HashSet<>(used);
    usedNow.addAll(next.used);
    Set<String> stillFromOutside = new HashSet<>(readsFromOutside);
    for (String name : next.readsFromOutside) {
      if (declared.containsKey(name)) {
        usedNow.add(name);
      } else {
        stillFromOutside.add(name);
      }
    }
    Map<String, DeclaredVariable> declaredInBoth = new HashMap<>(declared);
    declaredInBoth.putAll(next.declared);
    List<Violation> unusedInBoth = new ArrayList<>(unusedInClosedBlocks);
    unusedInBoth.addAll(next.unusedInClosedBlocks);
    return new VariableUsage(declaredInBoth, usedNow, stillFromOutside, unusedInBoth);
  }

  /**
   * Lo que se ve de este pedazo desde afuera cuando es un bloque que terminó: sus variables dejan
   * de existir, y las que nadie usó quedan como violaciones.
   */
  VariableUsage asClosedBlock() {
    return new VariableUsage(Map.of(), Set.of(), readsFromOutside, unused());
  }

  /** Las variables de este pedazo y de sus bloques que nadie usó, en la posición de su nombre. */
  public List<Violation> unused() {
    List<Violation> unused = new ArrayList<>(unusedInClosedBlocks);
    for (DeclaredVariable variable : declared.values()) {
      if (!used.contains(variable.name())) {
        unused.add(variable.neverUsed());
      }
    }
    return List.copyOf(unused);
  }
}
