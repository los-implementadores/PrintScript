package org.printscript.cli.io;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/** {@link FileAccess} sobre el sistema de archivos local. */
public class LocalFileAccess implements FileAccess {

  @Override
  public Reader openReader(String path) throws IOException {
    return Files.newBufferedReader(Path.of(path), StandardCharsets.UTF_8);
  }

  /**
   * Escribe en un archivo temporal al lado del original y, si todo sale bien, lo mueve encima. Así
   * el original se puede seguir leyendo mientras se escribe y nunca queda a medio escribir.
   */
  @Override
  public void rewrite(String path, ContentWriter content) throws IOException {
    Path target = Path.of(path).toAbsolutePath();
    if (!Files.exists(target)) {
      throw new NoSuchFileException(path);
    }
    Path temp = Files.createTempFile(target.getParent(), target.getFileName().toString(), ".tmp");
    try {
      try (Writer writer = Files.newBufferedWriter(temp, StandardCharsets.UTF_8)) {
        content.writeTo(writer);
      }
      replace(temp, target);
    } finally {
      Files.deleteIfExists(temp);
    }
  }

  private static void replace(Path source, Path target) throws IOException {
    try {
      Files.move(
          source, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    } catch (AtomicMoveNotSupportedException e) {
      Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
    }
  }
}
