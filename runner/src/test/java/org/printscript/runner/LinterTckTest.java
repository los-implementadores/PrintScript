package org.printscript.runner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.printscript.analyzer.violation.Violation;
import org.printscript.rules.RuleConfig;

/**
 * Los casos de linter del TCK, contra el runner. Como en el TCK, un caso que empieza con {@code
 * valid} no tiene que dar ninguna violación y uno que empieza con {@code invalid}, al menos una.
 *
 * <p>Los casos son copia de {@code src/test/resources/linter} del fork del TCK
 * (los-implementadores/printscript-tck @ 8318050). Los de 1.0 también corren en 1.1.
 */
class LinterTckTest {

  private static final AnalysisService ANALYSIS = LanguageServices.printScript().analysis();

  static Stream<Arguments> cases() throws IOException, URISyntaxException {
    Path base = Path.of(LinterTckTest.class.getResource("/tck/linter").toURI());
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
  void reportsWhatTheTckExpects(LanguageVersion version, String name, Path dir) {
    RuleConfig config = RuleConfig.fromJson(new StringReader(read(dir, "config.json")));

    List<Violation> violations =
        ANALYSIS.analyze(new StringReader(read(dir, "main.ps")), version, config);

    if (name.startsWith("valid")) {
      assertEquals(List.of(), violations);
    } else {
      assertFalse(violations.isEmpty(), "se esperaba al menos una violación");
    }
  }

  private static String read(Path dir, String file) {
    try {
      return Files.readString(dir.resolve(file), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
