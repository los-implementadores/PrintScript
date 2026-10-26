package org.printscript.interpreter.builtin;

import org.printscript.common.LanguageVersion;
import org.printscript.common.Position;
import org.printscript.common.ast.CallExpression;
import org.printscript.interpreter.ExecutionIo;
import org.printscript.interpreter.util.TypeCoercion;
import org.printscript.interpreter.util.ValueFormatter;

/**
 * Base de las funciones que leen un texto de afuera del programa y lo convierten al tipo que espera
 * el contexto: {@code let n: number = readInput("Edad: ");} devuelve un {@code number}.
 *
 * <p>Las subclases sólo dicen de dónde sale el texto.
 */
abstract class ExternalTextFunction implements BuiltinFunction {

  @Override
  public LanguageVersion since() {
    return LanguageVersion.V1_1;
  }

  @Override
  public int arity() {
    return 1;
  }

  @Override
  public String checkTypes(TypeChecker checker, CallExpression call) {
    String argumentType = checker.typeOf(call.getArguments().get(0), "string");
    if (!"string".equals(argumentType)) {
      throw checker.error(
          name() + " argument must be of type string, found " + argumentType, call.getPosition());
    }
    return resultType(checker.expectedType());
  }

  @Override
  public Object call(CallContext context, CallExpression call) {
    Object argument = context.evaluate(call.getArguments().get(0), "string");
    String raw = read(context.io(), ValueFormatter.display(argument), call.getPosition());
    return TypeCoercion.coerce(
        raw, resultType(context.expectedType()), call.getPosition(), sourceDescription());
  }

  /**
   * Lee el texto crudo.
   *
   * @param argument el argumento de la llamada ya evaluado (el prompt, el nombre de la variable)
   * @throws org.printscript.interpreter.ExecutionException si la fuente no tiene un valor
   */
  protected abstract String read(ExecutionIo io, String argument, Position position);

  /** Cómo se nombra la fuente en los errores de conversión (ej. {@code "input"}). */
  protected abstract String sourceDescription();

  private static String resultType(String expectedType) {
    return expectedType != null ? expectedType : "string";
  }
}
