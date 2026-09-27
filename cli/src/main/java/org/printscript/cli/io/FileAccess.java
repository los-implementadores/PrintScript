package org.printscript.cli.io;

import java.io.IOException;
import java.io.Reader;

/**
 * Acceso al sistema de archivos que necesitan los comandos (leer scripts/configs, sobreescribir).
 */
public interface FileAccess {

  Reader openReader(String path) throws IOException;

  void write(String path, String content) throws IOException;
}
