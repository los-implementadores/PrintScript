package org.printscript.common;

/**
 * Versión del lenguaje PrintScript soportada por el pipeline (lexer/parser/interpreter/formatter/
 * analyzer).
 *
 * <p>Es el mecanismo transversal sobre el que se apoyan las features de 1.1: cada módulo recibe una
 * {@code LanguageVersion} y habilita/deshabilita construcciones según corresponda. Por ejemplo, el
 * parser expone una gramática distinta por versión (ver {@code ParserGrammar}).
 */
public enum LanguageVersion {
  V1_0("1.0"),
  V1_1("1.1");

  private final String label;

  LanguageVersion(String label) {
    this.label = label;
  }

  /** Etiqueta textual de la versión, tal como la usa el TCK y el CLI (ej. {@code "1.1"}). */
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
    throw new IllegalArgumentException(
        "Unsupported PrintScript version: '" + label + "'. Supported: 1.0, 1.1");
  }
}
