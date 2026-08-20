package org.printscript.common.env;

import java.util.HashMap;
import java.util.Map;

public class Environment {

  private record Symbol(String type, Object value) {}

  private final Map<String, Symbol> values = new HashMap<>();

  /** Declara una nueva variable. Falla si ya estaba declarada en el Environment. */
  public void define(String name, String type, Object value) {
    if (values.containsKey(name)) {
      throw new RuntimeException("Variable '" + name + "' already declared.");
    }
    values.put(name, new Symbol(type, value));
  }

  /** Reasigna una variable existente. Falla si no existe. */
  public void assign(String name, Object value) {
    if (!values.containsKey(name)) {
      throw new RuntimeException("Variable '" + name + "' is not declared.");
    }
    Symbol current = values.get(name);
    values.put(name, new Symbol(current.type(), value));
  }

  /** Obtiene el valor de una variable. Falla si no existe. */
  public Object get(String name) {
    if (!values.containsKey(name)) {
      throw new RuntimeException("Variable '" + name + "' is not declared.");
    }
    return values.get(name).value();
  }

  /** Obtiene el tipo declarado de una variable ("number" o "string"). */
  public String getType(String name) {
    if (!values.containsKey(name)) {
      throw new RuntimeException("Variable '" + name + "' is not declared.");
    }
    return values.get(name).type();
  }

  public boolean isDeclared(String name) {
    return values.containsKey(name);
  }
}
