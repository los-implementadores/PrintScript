package org.printscript.runner;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.printscript.lexer.LexerFactory;
import org.printscript.rules.RuleConfig;
import org.printscript.token.Token;

/**
 * Los casos de formatter del TCK, contra el runner, y las propiedades que se le piden a un
 * formatter sobre el AST como rustfmt (ADR-029): idempotencia, sin reglas no cambia nada y solo
 * cambia espacios.
 *
 * <p>Los casos son copia de {@code src/test/resources/formatter} del fork del TCK
 * (los-implementadores/printscript-tck @ 8318050). Como en el TCK, los de 1.0 también corren en
 * 1.1.
 */
class FormatterGoldenTest {

  private static final FormattingService FORMATTING = LanguageServices.printScript().formatting();

  static Stream<Arguments> cases() throws IOException, URISyntaxException {
    Path base = Path.of(FormatterGoldenTest.class.getResource("/tck/formatter").toURI());
    List<Arguments> cases = new ArrayList<>();
    for (LanguageVersion version : LanguageVersion.values()) {
      for (LanguageVersion caseVersion : LanguageVersion.values()) {
        if (caseVersion.compareTo(version) > 0) {
          continue;
        }
        try (Stream<Path> dirs = Files.list(base.resolve(caseVersion.label()))) {
          dirs.sorted()
              .forEach(dir -> cases.add(Arguments.of(version, dir.getFileName().toString(), dir)));
        }
      }
    }
    return cases.stream();
  }

  @ParameterizedTest(name = "{0} {1}")
  @MethodSource("cases")
  void formatsLikeTheGolden(LanguageVersion version, String name, Path dir) {
    assertEquals(read(dir, "golden.ps"), format(read(dir, "main.ps"), version, config(dir)));
  }

  @ParameterizedTest(name = "{0} {1}")
  @MethodSource("cases")
  void formattingTheGoldenAgainChangesNothing(LanguageVersion version, String name, Path dir) {
    String golden = read(dir, "golden.ps");

    assertEquals(golden, format(golden, version, config(dir)));
  }

  @ParameterizedTest(name = "{0} {1}")
  @MethodSource("cases")
  void withoutRulesNothingChanges(LanguageVersion version, String name, Path dir) {
    String main = read(dir, "main.ps");

    assertEquals(main, format(main, version, RuleConfig.empty()));
  }

  @ParameterizedTest(name = "{0} {1}")
  @MethodSource("cases")
  void onlyTheWhitespaceChanges(LanguageVersion version, String name, Path dir) {
    String main = read(dir, "main.ps");

    assertEquals(lexemes(main), lexemes(format(main, version, config(dir))));
  }

  @ParameterizedTest(name = "{0} {1}")
  @MethodSource("cases")
  void theDefaultStyleIsIdempotent(LanguageVersion version, String name, Path dir) {
    RuleConfig defaults = FORMATTING.defaultRules();
    String once = format(read(dir, "main.ps"), version, defaults);

    assertEquals(once, format(once, version, defaults));
  }

  private static String format(String code, LanguageVersion version, RuleConfig rules) {
    StringWriter target = new StringWriter();
    FORMATTING.format(new StringReader(code), version, rules, target);
    return target.toString();
  }

  private static RuleConfig config(Path dir) {
    return RuleConfig.fromJson(new StringReader(read(dir, "config.json")));
  }

  private static String read(Path dir, String file) {
    try {
      return Files.readString(dir.resolve(file), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  /** Los tokens del código, sin los espacios: lo que tiene que quedar igual al formatear. */
  private static List<String> lexemes(String code) {
    List<String> lexemes = new ArrayList<>();
    Iterator<Token> tokens = LexerFactory.create(new StringReader(code));
    tokens.forEachRemaining(token -> lexemes.add(token.getType() + " " + token.getLexeme()));
    return lexemes;
  }
}
