package org.printscript.formatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Iterator;
import org.junit.jupiter.api.Test;

class StreamingFormatterTest {

  private Formatter formatter(String json) {
    FormattingRules rules = FormattingRules.fromJson(new StringReader(json));
    return new FormatterImpl(rules);
  }

  @Test
  void formatsAssignSpacing() {
    String input = "let x: number= 5;\nlet y: number =10;";

    Formatter withSpaces = formatter("{\"enforce-spacing-around-equals\": true}");
    assertEquals("let x: number = 5;\nlet y: number = 10;", withSpaces.format(input));

    Formatter withoutSpaces = formatter("{\"enforce-no-spacing-around-equals\": true}");
    assertEquals("let x: number=5;\nlet y: number=10;", withoutSpaces.format(input));
  }

  @Test
  void formatsColonSpacing() {
    String input = "let a:string = \"x\";\nlet b : string = \"y\";";

    Formatter afterColon = formatter("{\"enforce-spacing-after-colon-in-declaration\": true}");
    assertEquals("let a: string = \"x\";\nlet b : string = \"y\";", afterColon.format(input));

    Formatter beforeColon = formatter("{\"enforce-spacing-before-colon-in-declaration\": true}");
    assertEquals("let a :string = \"x\";\nlet b : string = \"y\";", beforeColon.format(input));
  }

  @Test
  void formatsOperationSpacing() {
    String input = "let r: number = 5+4*3/2-1;";
    Formatter fmt = formatter("{\"mandatory-space-surrounding-operations\": true}");
    assertEquals("let r: number = 5 + 4 * 3 / 2 - 1;", fmt.format(input));
  }

  @Test
  void formatsLineBreakAfterStatement() {
    String input = "let a: number = 1;let b: number = 2;";
    Formatter fmt = formatter("{\"mandatory-line-break-after-statement\": true}");
    assertEquals("let a: number = 1;\nlet b: number = 2;", fmt.format(input));
  }

  @Test
  void formatsSingleSpaceSeparation() {
    String input = "let   something:   string=\"hello\";\nprintln(something);";
    Formatter fmt = formatter("{\"mandatory-single-space-separation\": true}");
    assertEquals("let something : string = \"hello\";\nprintln ( something );", fmt.format(input));
  }

  @Test
  void formatsIfBracesAndIndentation() {
    String sameLineInput = "if (true)\n{\n  println(\"ok\");\n}";
    Formatter sameLine = formatter("{\"if-brace-same-line\": true}");
    assertEquals("if (true) {\n  println(\"ok\");\n}", sameLine.format(sameLineInput));

    String belowLineInput = "if (true) {\n  println(\"ok\");\n}";
    Formatter belowLine = formatter("{\"if-brace-below-line\": true}");
    assertEquals("if (true)\n{\n  println(\"ok\");\n}", belowLine.format(belowLineInput));

    String nestedInput = "if (a) {\n  if (b) {\n    println(\"nested\");\n  }\n}";
    Formatter indentFmt = formatter("{\"indent-inside-if\": 4}");
    assertEquals(
        "if (a) {\n    if (b) {\n        println(\"nested\");\n    }\n}",
        indentFmt.format(nestedInput));
  }

  @Test
  void formatsPrintlnLineBreaks() {
    String input = "let x: number = 1;\nprintln(x);\n\n\nprintln(\"end\");";

    Formatter zeroBreaks = formatter("{\"line-breaks-after-println\": 0}");
    assertEquals("let x: number = 1;\nprintln(x);\nprintln(\"end\");", zeroBreaks.format(input));

    Formatter oneBreak = formatter("{\"line-breaks-after-println\": 1}");
    assertEquals("let x: number = 1;\nprintln(x);\n\nprintln(\"end\");", oneBreak.format(input));

    Formatter twoBreaks = formatter("{\"line-breaks-after-println\": 2}");
    assertEquals("let x: number = 1;\nprintln(x);\n\n\nprintln(\"end\");", twoBreaks.format(input));
  }

  @Test
  void formatsStreamingFromReaderToWriter() {
    String input = "let a: number = 1;let b: number = 2;";
    Formatter fmt = formatter("{\"mandatory-line-break-after-statement\": true}");

    StringWriter writer = new StringWriter();
    fmt.format(new StringReader(input), writer);

    assertEquals("let a: number = 1;\nlet b: number = 2;", writer.toString());
  }

  @Test
  void formatsLazyIterator() {
    String input = "let a: number = 1;\nlet b: number = 2;";
    Formatter fmt = formatter("{}");

    Iterator<String> it = fmt.formatIterator(new StringReader(input));
    assertTrue(it.hasNext());
    assertEquals("let a: number = 1;", it.next());
    assertTrue(it.hasNext());
    assertEquals("let b: number = 2;", it.next());
    assertFalse(it.hasNext());
  }

  @Test
  void preservesUnconfiguredFormatting() {
    String input = "let x:string = 5;\nlet y : string=10;";
    // Solo activamos espacio tras los dos puntos: debe preservar el espacio previo a ':' y el '='
    // pegado
    Formatter fmt = formatter("{\"enforce-spacing-after-colon-in-declaration\": true}");
    assertEquals("let x: string = 5;\nlet y : string=10;", fmt.format(input));
  }
}
