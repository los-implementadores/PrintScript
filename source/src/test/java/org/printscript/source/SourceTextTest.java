package org.printscript.source;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import org.junit.jupiter.api.Test;

class SourceTextTest {

  @Test
  void readsWhitespaceAndKnownTextInOrder() {
    SourceText source = SourceText.of("  let\t x\r\n");

    assertEquals("  ", source.whitespace());
    source.expect("let");
    assertEquals("\t ", source.whitespace());
    source.expect("x");
    assertFalse(source.atEnd());
    assertEquals("\r\n", source.whitespace());
    assertTrue(source.atEnd());
  }

  @Test
  void noWhitespaceIsAnEmptyString() {
    SourceText source = SourceText.of("x;");

    assertEquals("", source.whitespace());
    source.expect("x");
    assertEquals("", source.whitespace());
  }

  @Test
  void expectingSomethingElseFails() {
    SourceText source = SourceText.of("let x");

    IllegalStateException error =
        assertThrows(IllegalStateException.class, () -> source.expect("const"));
    assertEquals("El código original no sigue con 'const' sino con 'let x'", error.getMessage());
    assertThrows(IllegalStateException.class, () -> SourceText.of("le").expect("let"));
  }

  @Test
  void ofGivesTheWholeTextToTheReader() throws IOException {
    Reader reader = SourceText.of("abc").reader();

    char[] buffer = new char[3];
    assertEquals(3, reader.read(buffer, 0, 3));
    assertEquals("abc", new String(buffer));
  }

  @Test
  void recordingKeepsWhatTheReaderRead() throws IOException {
    SourceText source = SourceText.recording(new StringReader("let x;"));
    Reader reader = source.reader();

    assertTrue(source.atEnd());
    assertEquals('l', reader.read());
    char[] buffer = new char[10];
    assertEquals(5, reader.read(buffer, 0, 10));
    assertEquals(-1, reader.read());
    assertEquals(-1, reader.read(buffer, 0, 10));

    source.expect("let");
    assertEquals(" ", source.whitespace());
    source.expect("x;");
    assertTrue(source.atEnd());
  }

  @Test
  void whatTheCursorPassedIsReleasedWithoutLosingTheRest() throws IOException {
    String statement = "println(1);\n";
    String program = statement.repeat(2000);
    SourceText source = SourceText.recording(new StringReader(program));
    Reader reader = source.reader();
    int read = 0;
    while (reader.read() != -1) {
      read++;
    }
    assertEquals(program.length(), read);

    for (int i = 0; i < 2000; i++) {
      source.expect("println(1);");
      assertEquals("\n", source.whitespace());
    }
    assertTrue(source.atEnd());
  }

  @Test
  void whitespaceIsWhatTheLexerSkips() {
    assertTrue(SourceText.isWhitespace(' '));
    assertTrue(SourceText.isWhitespace('\t'));
    assertTrue(SourceText.isWhitespace('\r'));
    assertTrue(SourceText.isWhitespace('\n'));
    assertFalse(SourceText.isWhitespace('a'));
    assertFalse(SourceText.isWhitespace(';'));
  }
}
