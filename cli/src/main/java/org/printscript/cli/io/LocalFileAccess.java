package org.printscript.cli.io;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** {@link FileAccess} sobre el sistema de archivos local. */
public class LocalFileAccess implements FileAccess {

  @Override
  public Reader openReader(String path) throws IOException {
    return Files.newBufferedReader(Path.of(path), StandardCharsets.UTF_8);
  }

  @Override
  public void write(String path, String content) throws IOException {
    Files.writeString(Path.of(path), content, StandardCharsets.UTF_8);
  }
}
