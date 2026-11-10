package org.printscript.runner;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.printscript.interpreter.builtin.BuiltinFunction;
import org.printscript.interpreter.builtin.BuiltinFunctions;
import org.printscript.interpreter.builtin.PrintlnFunction;
import org.printscript.interpreter.builtin.ReadEnvFunction;
import org.printscript.interpreter.builtin.ReadInputFunction;
import org.printscript.parser.GrammarFeature;
import org.printscript.parser.ParserGrammar;

/**
 * Versión de PrintScript que se quiere usar.
 *
 * <p>Es el único lugar del proyecto que sabe que el lenguaje tiene versiones y qué trae cada una:
 * qué construcciones entiende el parser y qué funciones nativas existen. Los módulos (lexer,
 * parser, intérprete, formatter, linter) no conocen las versiones: el runner les pasa la gramática
 * y las funciones ya armadas (ver ADR-025). Agregar una versión es agregar una constante acá.
 */
public enum LanguageVersion {
  V1_0("1.0", List.of(GrammarFeature.CORE), List.of(new PrintlnFunction())),

  V1_1(
      "1.1",
      List.of(
          GrammarFeature.CORE,
          GrammarFeature.BOOLEANS,
          GrammarFeature.CONSTANTS,
          GrammarFeature.CONDITIONALS),
      List.of(new PrintlnFunction(), new ReadInputFunction(), new ReadEnvFunction()));

  private final String label;
  private final List<GrammarFeature> grammar;
  private final List<BuiltinFunction> builtins;

  LanguageVersion(String label, List<GrammarFeature> grammar, List<BuiltinFunction> builtins) {
    this.label = label;
    this.grammar = grammar;
    this.builtins = builtins;
  }

  /** Etiqueta textual de la versión, tal como la usan el TCK y la CLI (ej. {@code "1.1"}). */
  public String label() {
    return label;
  }

  /**
   * Resuelve una {@link LanguageVersion} a partir de su etiqueta textual ({@code "1.0"} o {@code
   * "1.1"}).
   *
   * @param label etiqueta de versión
   * @return la versión correspondiente
   * @throws IllegalArgumentException si la etiqueta no corresponde a una versión soportada
   */
  public static LanguageVersion fromLabel(String label) {
    for (LanguageVersion version : values()) {
      if (version.label.equals(label)) {
        return version;
      }
    }
    String supported =
        Arrays.stream(values()).map(LanguageVersion::label).collect(Collectors.joining(", "));
    throw new IllegalArgumentException(
        "Unsupported PrintScript version: '" + label + "'. Supported: " + supported);
  }

  /** Lo que entiende el parser en esta versión. */
  ParserGrammar grammar() {
    return ParserGrammar.of(grammar.toArray(GrammarFeature[]::new));
  }

  /** Las funciones nativas que existen en esta versión. */
  BuiltinFunctions builtins() {
    return new BuiltinFunctions(builtins);
  }
}
