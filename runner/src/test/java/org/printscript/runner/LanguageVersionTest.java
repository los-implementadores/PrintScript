package org.printscript.runner;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
import org.junit.jupiter.api.Test;
import org.printscript.interpreter.semantic.SemanticException;
import org.printscript.parser.ParseException;

/** Qué acepta cada versión del lenguaje: lo decide el runner, no los módulos. */
class LanguageVersionTest {

  private final ValidationService validation = LanguageServices.printScript().validation();

  private void validate(String source, LanguageVersion version) {
    validation.validate(new StringReader(source), version);
  }

  @Test
  void fromLabelResolvesSupportedVersions() {
    assertEquals(LanguageVersion.V1_0, LanguageVersion.fromLabel("1.0"));
    assertEquals(LanguageVersion.V1_1, LanguageVersion.fromLabel("1.1"));
    assertEquals("1.1", LanguageVersion.V1_1.label());
  }

  @Test
  void fromLabelRejectsUnsupportedVersionAndListsTheSupportedOnes() {
    IllegalArgumentException e =
        assertThrows(IllegalArgumentException.class, () -> LanguageVersion.fromLabel("2.0"));
    assertTrue(e.getMessage().endsWith("Supported: 1.0, 1.1"));
  }

  @Test
  void version10RejectsTheConstructsOf11() {
    assertThrows(
        ParseException.class, () -> validate("let ok: boolean = true;", LanguageVersion.V1_0));
    assertThrows(
        ParseException.class, () -> validate("const x: number = 1;", LanguageVersion.V1_0));
    assertThrows(ParseException.class, () -> validate("if (x) { }", LanguageVersion.V1_0));
  }

  @Test
  void version10DoesNotHaveTheFunctionsOf11() {
    SemanticException e =
        assertThrows(
            SemanticException.class,
            () -> validate("let n: string = readInput(\"x\");", LanguageVersion.V1_0));
    assertTrue(e.getMessage().contains("Unknown function 'readInput'"));
    assertThrows(
        SemanticException.class,
        () -> validate("let n: string = readEnv(\"HOME\");", LanguageVersion.V1_0));
  }

  @Test
  void version11AcceptsEverything() {
    assertDoesNotThrow(
        () ->
            validate(
                "const ok: boolean = true;\n"
                    + "let name: string = readInput(\"Nombre: \");\n"
                    + "let home: string = readEnv(\"HOME\");\n"
                    + "if (ok) { println(name + home); } else { println(1 + 2); }",
                LanguageVersion.V1_1));
  }
}
