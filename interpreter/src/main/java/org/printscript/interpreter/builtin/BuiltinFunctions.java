package org.printscript.interpreter.builtin;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.printscript.common.LanguageVersion;
import org.printscript.common.ast.CallExpression;

/**
 * Funciones nativas disponibles, indexadas por nombre.
 *
 * <p>Agregar una función al lenguaje es escribir una {@link BuiltinFunction} y registrarla en
 * {@link #standard()}: ni el análisis semántico ni el intérprete cambian.
 */
public final class BuiltinFunctions {

  private final Map<String, BuiltinFunction> functions = new LinkedHashMap<>();

  public BuiltinFunctions(List<BuiltinFunction> functions) {
    for (BuiltinFunction function : functions) {
      this.functions.put(function.name(), function);
    }
  }

  /** Las funciones de PrintScript: {@code println}, {@code readInput} y {@code readEnv}. */
  public static BuiltinFunctions standard() {
    return new BuiltinFunctions(
        List.of(new PrintlnFunction(), new ReadInputFunction(), new ReadEnvFunction()));
  }

  /**
   * Busca la función de una llamada y verifica que exista en la versión y que la cantidad de
   * argumentos coincida.
   *
   * <p>Las reglas son las mismas para el análisis semántico y para el intérprete; cada uno decide
   * qué excepción lanzar a partir del detalle del error.
   *
   * @param error construye la excepción a partir del detalle del error
   * @return la función, ya validada
   */
  public <E extends RuntimeException> BuiltinFunction resolve(
      CallExpression call, LanguageVersion version, Function<String, E> error) {
    BuiltinFunction function = functions.get(call.getCallee());
    if (function == null) {
      throw error.apply("Unknown function '" + call.getCallee() + "'");
    }
    // Las versiones están declaradas en orden en LanguageVersion.
    if (version.compareTo(function.since()) < 0) {
      throw error.apply(
          "Function '"
              + function.name()
              + "' is only supported in PrintScript "
              + function.since().label()
              + ".");
    }
    int arity = function.arity();
    if (call.getArguments().size() != arity) {
      throw error.apply(
          function.name() + " expects " + arity + " argument" + (arity == 1 ? "" : "s") + ".");
    }
    return function;
  }
}
