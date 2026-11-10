package org.printscript.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

class RuleRegistryTest {

  /** Una "regla" de prueba: solo recuerda con qué se armó. */
  private record Rule(String description) {}

  private final RuleRegistry<Rule> registry =
      RuleRegistry.<Rule>builder()
          .rule("flag", "tck-flag")
          .whenOn(() -> new Rule("flag"))
          .rule("spaces")
          .withBoolean(on -> new Rule(on ? "with spaces" : "without spaces"))
          .rule("indent")
          .withInt(size -> new Rule("indent " + size))
          .rule("format")
          .withText(format -> new Rule("format " + format))
          .rule("custom")
          .with(
              setting ->
                  Optional.of(new Rule(setting.key() + " " + setting.config().text("extra", "-"))))
          .build();

  private List<Rule> create(Map<String, Object> values) {
    return registry.create(RuleConfig.of(values));
  }

  @Test
  void anEmptyConfigActivatesNothing() {
    assertTrue(registry.create(RuleConfig.empty()).isEmpty());
  }

  @Test
  void eachKeyBuildsItsRuleInRegistrationOrder() {
    Map<String, Object> values = new HashMap<>();
    values.put("format", "snake");
    values.put("indent", 4);
    values.put("spaces", false);
    values.put("flag", true);
    values.put("custom", 1);
    values.put("extra", "x");

    assertEquals(
        List.of(
            new Rule("flag"),
            new Rule("without spaces"),
            new Rule("indent 4"),
            new Rule("format snake"),
            new Rule("custom x")),
        create(values));
  }

  @Test
  void aFlagIsOffWhenFalseOrNull() {
    Map<String, Object> nullFlag = new HashMap<>();
    nullFlag.put("flag", null);

    assertTrue(create(Map.of("flag", false)).isEmpty());
    assertTrue(create(nullFlag).isEmpty());
  }

  @Test
  void aliasesBuildASingleRule() {
    assertEquals(List.of(new Rule("flag")), create(Map.of("tck-flag", true)));
    assertEquals(List.of(new Rule("flag")), create(Map.of("flag", true, "tck-flag", true)));
  }

  @Test
  void unknownKeysAreIgnored() {
    assertTrue(create(Map.of("some-future-rule", true)).isEmpty());
  }

  @Test
  void aValueOfTheWrongTypeIsRejected() {
    IllegalArgumentException e =
        assertThrows(IllegalArgumentException.class, () -> create(Map.of("indent", "four")));
    assertEquals("La regla 'indent' espera un número entero, pero recibió four", e.getMessage());
    assertThrows(IllegalArgumentException.class, () -> create(Map.of("flag", "yes")));
    assertThrows(IllegalArgumentException.class, () -> create(Map.of("format", 3)));
  }

  @Test
  void eachCallBuildsNewRules() {
    RuleRegistry<Object> stateful =
        RuleRegistry.builder().rule("state").whenOn(Object::new).build();
    RuleConfig config = RuleConfig.of(Map.of("state", true));

    assertNotSame(stateful.create(config).get(0), stateful.create(config).get(0));
  }

  @Test
  void listsAllItsKeys() {
    assertEquals(
        Set.of("flag", "tck-flag", "spaces", "indent", "format", "custom"), registry.keys());
  }
}
