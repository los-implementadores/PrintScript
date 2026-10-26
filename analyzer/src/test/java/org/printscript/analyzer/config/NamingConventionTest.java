package org.printscript.analyzer.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
import java.util.List;
import org.junit.jupiter.api.Test;

class NamingConventionTest {

  @Test
  void camelCaseAcceptsDigitsAndAcronyms() {
    for (String name : List.of("x", "value1", "myVar", "myVar2Name", "myURL")) {
      assertTrue(NamingConvention.CAMEL_CASE.matches(name), name);
    }
    for (String name : List.of("MiVar", "my_var", "Value1")) {
      assertFalse(NamingConvention.CAMEL_CASE.matches(name), name);
    }
  }

  @Test
  void snakeCaseAcceptsDigits() {
    for (String name : List.of("x", "var1", "my_var", "my_var_2")) {
      assertTrue(NamingConvention.SNAKE_CASE.matches(name), name);
    }
    for (String name : List.of("myVar", "my__var", "_x", "var_", "My_var")) {
      assertFalse(NamingConvention.SNAKE_CASE.matches(name), name);
    }
  }

  @Test
  void nullFormatInJsonKeepsTheDefault() {
    AnalyzerConfig config =
        AnalyzerConfig.fromJson(
            new StringReader(
                "{\"activeNamingConvention\": true, \"namingConventionFormat\": null}"));

    assertTrue(config.activeNamingConvention);
    assertEquals(NamingConvention.CAMEL_CASE, config.namingConventionFormat);
  }
}
