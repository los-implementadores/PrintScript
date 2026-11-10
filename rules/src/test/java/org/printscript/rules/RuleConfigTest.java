package org.printscript.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Reader;
import java.io.StringReader;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/** Fija cómo se leen los archivos de configuración de reglas. */
class RuleConfigTest {

  private RuleConfig parse(String json) {
    return RuleConfig.fromJson(new StringReader(json));
  }

  @Test
  void readsTheValuesOfEachKey() {
    RuleConfig config = parse("{\"a\": true, \"b\": 2, \"c\": \"camel case\", \"d\": null}");

    assertEquals(Optional.of(true), config.get("a"));
    assertEquals(Optional.of(2), config.get("b"));
    assertEquals("camel case", config.text("c", "x"));
    assertTrue(config.has("d"));
    assertEquals(Optional.empty(), config.get("d"));
    assertFalse(config.has("e"));
  }

  @Test
  void aMissingOrNullTextKeyFallsBack() {
    RuleConfig config = parse("{\"format\": null}");

    assertEquals("camelCase", config.text("format", "camelCase"));
    assertEquals("camelCase", config.text("other", "camelCase"));
  }

  @Test
  void rejectsMalformedJson() {
    assertThrows(IllegalArgumentException.class, () -> parse("{\"indentSize\": "));
  }

  @Test
  void rejectsSomethingThatIsNotAnObject() {
    assertThrows(IllegalArgumentException.class, () -> parse("[1, 2]"));
    assertThrows(IllegalArgumentException.class, () -> parse("null"));
  }

  @Test
  void rejectsContentAfterTheJson() {
    assertThrows(IllegalArgumentException.class, () -> parse("{\"indentSize\": 2} basura"));
  }

  @Test
  void acceptsOnlyStandardJson() {
    assertThrows(IllegalArgumentException.class, () -> parse("{'indentSize': 2}"));
    assertThrows(IllegalArgumentException.class, () -> parse("{indentSize: 2}"));
    assertThrows(
        IllegalArgumentException.class, () -> parse("{ // comentario\n \"indentSize\": 2}"));
  }

  @Test
  void doesNotCloseTheReader() {
    boolean[] closed = {false};
    Reader reader =
        new StringReader("{\"indentSize\": 2}") {
          @Override
          public void close() {
            closed[0] = true;
            super.close();
          }
        };

    RuleConfig.fromJson(reader);

    assertFalse(closed[0]);
  }
}
