package org.printscript.cli.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LocalFileAccessTest {

  @TempDir Path dir;

  private final LocalFileAccess files = new LocalFileAccess();

  private long filesIn(Path directory) throws IOException {
    try (Stream<Path> entries = Files.list(directory)) {
      return entries.count();
    }
  }

  @Test
  void rewriteReplacesTheContentAndLeavesNoTemporaryFile() throws IOException {
    Path script = Files.writeString(dir.resolve("script.ps"), "viejo", StandardCharsets.UTF_8);

    files.rewrite(script.toString(), target -> target.write("nuevo"));

    assertEquals("nuevo", Files.readString(script));
    assertEquals(1, filesIn(dir));
  }

  @Test
  void rewriteCanReadTheOriginalWhileWriting() throws IOException {
    Path script = Files.writeString(dir.resolve("script.ps"), "abc", StandardCharsets.UTF_8);

    files.rewrite(
        script.toString(),
        target -> {
          try (var original = files.openReader(script.toString())) {
            original.transferTo(target);
            target.write("!");
          }
        });

    assertEquals("abc!", Files.readString(script));
  }

  @Test
  void aFailedRewriteKeepsTheOriginalFile() throws IOException {
    Path script = Files.writeString(dir.resolve("script.ps"), "original", StandardCharsets.UTF_8);

    assertThrows(
        IOException.class,
        () ->
            files.rewrite(
                script.toString(),
                target -> {
                  target.write("a medias");
                  throw new IOException("falla a mitad de la escritura");
                }));

    assertEquals("original", Files.readString(script));
    assertEquals(1, filesIn(dir));
  }

  @Test
  void rewritingAMissingFileFails() {
    String missing = dir.resolve("nope.ps").toString();

    assertThrows(NoSuchFileException.class, () -> files.rewrite(missing, target -> {}));
  }
}
