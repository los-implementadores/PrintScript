package org.printscript.cli.io;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

/**
 * Acceso al sistema de archivos que necesitan los comandos (leer scripts/configs, sobreescribir).
 */
public interface FileAccess {

  Reader openReader(String path) throws IOException;

  /**
   * Reemplaza el contenido de un archivo existente, escribiéndolo de a partes. El archivo cambia
   * recién cuando {@code content} termina sin errores; si falla, queda como estaba.
   *
   * @throws java.nio.file.NoSuchFileException si el archivo no existe
   */
  void rewrite(String path, ContentWriter content) throws IOException;

  /** Escribe el contenido nuevo de un archivo. */
  @FunctionalInterface
  interface ContentWriter {
    void writeTo(Writer target) throws IOException;
  }
}
