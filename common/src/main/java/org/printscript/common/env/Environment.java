package org.printscript.common.env;

import java.util.HashMap;
import java.util.Map;

/**
 * Variables de un scope.
 *
 * <p>Cada bloque ({@code if}/{@code else}) trabaja en un scope hijo ({@link #childScope()}), como
 * en TypeScript: ve y puede reasignar las variables de los scopes de afuera, puede declarar las
 * suyas (incluso con un nombre que ya existe afuera) y lo que declara deja de existir cuando el
 * bloque termina.
 */
public class Environment {

  private record Symbol(String type, Object value, boolean isConst) {}

  /** Scope que contiene a este, o {@code null} si es el scope global. */
  private final Environment enclosing;

  private final Map<String, Symbol> values = new HashMap<>();

  /** Crea el scope global de un programa. */
  public Environment() {
    this(null);
  }

  private Environment(Environment enclosing) {
    this.enclosing = enclosing;
  }

  /** Crea el scope de un bloque anidado en este. */
  public Environment childScope() {
    return new Environment(this);
  }

  /** Declara una nueva variable mutable. Falla si ya estaba declarada en este scope. */
  public void define(String name, String type, Object value) {
    define(name, type, value, false);
  }

  /**
   * Declara una nueva variable, marcándola opcionalmente como constante (inmutable). Falla si ya
   * estaba declarada en este scope; si existe en un scope de afuera, la nueva la tapa.
   */
  public void define(String name, String type, Object value, boolean isConst) {
    if (values.containsKey(name)) {
      throw new RuntimeException("Variable '" + name + "' already declared.");
    }
    values.put(name, new Symbol(type, value, isConst));
  }

  /** Reasigna una variable existente en este scope o en uno de afuera. */
  public void assign(String name, Object value) {
    Environment owner = scopeDeclaring(name);
    Symbol current = owner.values.get(name);
    if (current.isConst()) {
      throw new RuntimeException("Cannot reassign constant '" + name + "'.");
    }
    owner.values.put(name, new Symbol(current.type(), value, current.isConst()));
  }

  /** {@code true} si la variable fue declarada como constante ({@code const}). */
  public boolean isConst(String name) {
    return lookup(name).isConst();
  }

  /** Obtiene el valor de una variable. Falla si no existe. */
  public Object get(String name) {
    return lookup(name).value();
  }

  /** Obtiene el tipo declarado de una variable ("number", "string" o "boolean"). */
  public String getType(String name) {
    return lookup(name).type();
  }

  /** {@code true} si la variable es visible desde este scope (acá o en uno de afuera). */
  public boolean isDeclared(String name) {
    return findScopeDeclaring(name) != null;
  }

  /** {@code true} si la variable fue declarada en este mismo scope. */
  public boolean isDeclaredInThisScope(String name) {
    return values.containsKey(name);
  }

  private Symbol lookup(String name) {
    return scopeDeclaring(name).values.get(name);
  }

  private Environment scopeDeclaring(String name) {
    Environment owner = findScopeDeclaring(name);
    if (owner == null) {
      throw new RuntimeException("Variable '" + name + "' is not declared.");
    }
    return owner;
  }

  private Environment findScopeDeclaring(String name) {
    for (Environment scope = this; scope != null; scope = scope.enclosing) {
      if (scope.values.containsKey(name)) {
        return scope;
      }
    }
    return null;
  }
}
