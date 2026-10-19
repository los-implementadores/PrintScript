package org.printscript.interpreter.builtin;

import org.printscript.common.LanguageVersion;
import org.printscript.common.ast.CallExpression;
import org.printscript.interpreter.util.ValueFormatter;

/** {@code println(expr)}: escribe el valor de la expresión en la salida del programa. */
final class PrintlnFunction implements BuiltinFunction {

  @Override
  public String name() {
    return "println";
  }

  @Override
  public LanguageVersion since() {
    return LanguageVersion.V1_0;
  }

  @Override
  public int arity() {
    return 1;
  }

  @Override
  public String checkTypes(TypeChecker checker, CallExpression call) {
    checker.typeOf(call.getArguments().get(0), "string");
    return null;
  }

  @Override
  public Object call(CallContext context, CallExpression call) {
    Object value = context.evaluate(call.getArguments().get(0), "string");
    context.io().output().print(ValueFormatter.display(value));
    return null;
  }
}
