package org.printscript.analyzer.rule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.printscript.analyzer.violation.Violation;
import org.printscript.ast.ASTVisitor;
import org.printscript.ast.AssignmentStatement;
import org.printscript.ast.BinaryExpression;
import org.printscript.ast.Block;
import org.printscript.ast.BooleanLiteral;
import org.printscript.ast.CallExpression;
import org.printscript.ast.Expression;
import org.printscript.ast.ExpressionStatement;
import org.printscript.ast.GroupExpression;
import org.printscript.ast.Identifier;
import org.printscript.ast.IfStatement;
import org.printscript.ast.NumberLiteral;
import org.printscript.ast.Statement;
import org.printscript.ast.StringLiteral;
import org.printscript.ast.VarDeclarationStatement;
import org.printscript.source.Position;

/**
 * Qué hace un pedazo de código con las variables: cuáles lee de afuera, cuál declara y cuáles de
 * sus bloques no usó nadie. Cada nodo arma su respuesta con la de sus hijos y no se guarda nada
 * entre una visita y otra.
 *
 * <p>Un bloque se resuelve entero acá, porque empieza y termina adentro de su sentencia: sus
 * variables sin usar ya se saben al salir de él. Lo que lee de afuera sube para que lo resuelva
 * quien lo contiene.
 */
final class VariableUsage implements ASTVisitor<VariableUsage.Usage> {

  static final VariableUsage INSTANCE = new VariableUsage();

  private VariableUsage() {}

  /**
   * Lo que hace un pedazo de código con las variables.
   *
   * @param reads las variables que lee o reasigna y que no se declaran adentro de ese pedazo
   * @param declares la variable que declara, si es una declaración
   * @param unused las variables declaradas en sus bloques que nadie usó
   */
  record Usage(Set<String> reads, Optional<Declared> declares, List<Violation> unused) {

    static final Usage NONE = new Usage(Set.of(), Optional.empty(), List.of());

    Usage {
      reads = Set.copyOf(reads);
      unused = List.copyOf(unused);
    }

    static Usage reading(String name) {
      return new Usage(Set.of(name), Optional.empty(), List.of());
    }

    /** Lo que hacen este pedazo y {@code other} juntos. Solo se junta lo que no declara nada. */
    Usage and(Usage other) {
      Set<String> allReads = new LinkedHashSet<>(reads);
      allReads.addAll(other.reads);
      List<Violation> allUnused = new ArrayList<>(unused);
      allUnused.addAll(other.unused);
      return new Usage(allReads, Optional.empty(), allUnused);
    }
  }

  /** Una variable declarada: su nombre y dónde está escrito. */
  record Declared(String name, Position position) {

    Violation neverUsed() {
      return new Violation("Variable '" + name + "' is declared but never used.", position);
    }
  }

  // ---------------------------------------------------------------- Sentencias

  /** El valor se lee antes de que la variable exista: en {@code let x = x + 1;} la x es otra. */
  @Override
  public Usage visitVarDeclaration(VarDeclarationStatement node) {
    Expression initializer = node.getInitializer();
    Set<String> reads = initializer == null ? Set.of() : initializer.accept(this).reads();
    Identifier name = node.getName();
    return new Usage(
        reads, Optional.of(new Declared(name.getName(), name.getPosition())), List.of());
  }

  /** Reasignar una variable cuenta como usarla. */
  @Override
  public Usage visitAssignment(AssignmentStatement node) {
    return Usage.reading(node.getTarget().getName()).and(node.getValue().accept(this));
  }

  @Override
  public Usage visitExpressionStatement(ExpressionStatement node) {
    return node.getExpression().accept(this);
  }

  @Override
  public Usage visitIf(IfStatement node) {
    Usage usage = node.getCondition().accept(this).and(node.getThenBlock().accept(this));
    return node.hasElse() ? usage.and(node.getElseBlock().accept(this)) : usage;
  }

  /**
   * Recorre las sentencias en orden, con las variables que se van declarando en el bloque. Cada
   * sentencia primero lee y después declara, así una variable no se usa a sí misma. Lo que se lee y
   * no se declaró en el bloque es de afuera.
   */
  @Override
  public Usage visitBlock(Block node) {
    Map<String, Declared> declared = new HashMap<>();
    Set<String> used = new HashSet<>();
    Set<String> fromOutside = new LinkedHashSet<>();
    List<Violation> unused = new ArrayList<>();
    for (Statement statement : node.getStatements()) {
      Usage usage = statement.accept(this);
      for (String name : usage.reads()) {
        (declared.containsKey(name) ? used : fromOutside).add(name);
      }
      unused.addAll(usage.unused());
      usage.declares().ifPresent(declaration -> declared.put(declaration.name(), declaration));
    }
    declared.values().stream()
        .filter(declaration -> !used.contains(declaration.name()))
        .map(Declared::neverUsed)
        .forEach(unused::add);
    return new Usage(fromOutside, Optional.empty(), unused);
  }

  // ---------------------------------------------------------------- Expresiones

  @Override
  public Usage visitIdentifier(Identifier node) {
    return Usage.reading(node.getName());
  }

  @Override
  public Usage visitBinaryExpression(BinaryExpression node) {
    return node.getLeft().accept(this).and(node.getRight().accept(this));
  }

  @Override
  public Usage visitCallExpression(CallExpression node) {
    return node.getArguments().stream()
        .map(argument -> argument.accept(this))
        .reduce(Usage.NONE, Usage::and);
  }

  @Override
  public Usage visitGroup(GroupExpression node) {
    return node.getExpression().accept(this);
  }

  @Override
  public Usage visitNumberLiteral(NumberLiteral node) {
    return Usage.NONE;
  }

  @Override
  public Usage visitStringLiteral(StringLiteral node) {
    return Usage.NONE;
  }

  @Override
  public Usage visitBooleanLiteral(BooleanLiteral node) {
    return Usage.NONE;
  }
}
