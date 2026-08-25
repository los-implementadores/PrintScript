package org.printscript.cli;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MainE2ETest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @TempDir
    Path tempDir;

    @BeforeEach
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    public void restoreStreams() {
        System.setOut(originalOut);
    }

    // Helper para obtener fácilmente las rutas de src/test/resources
    private Path getResourcePath(String fileName) throws Exception {
        URL resource = getClass().getClassLoader().getResource(fileName);
        if (resource == null) {
            throw new IllegalArgumentException("No se encontró el archivo en resources: " + fileName);
        }
        return Paths.get(resource.toURI());
    }

    @Test
    void testAnalyzerDetectsComplexPrintln() throws Exception {
        // 1. Obtenemos las rutas absolutas de los archivos en resources
        Path scriptPath = getResourcePath("analyzer_script.ps");
        Path configPath = getResourcePath("linter_rules.json");

        // 2. Ejecutamos el CLI directamente sobre los resources (el linter no modifica archivos)
        String[] args = { "analyzing", scriptPath.toString(), "--config", configPath.toString() };
        Main.main(args);

        // 3. Verificamos la salida
        String consoleOutput = outContent.toString();
        assertTrue(consoleOutput.contains("Linter found violations:"));
        assertTrue(consoleOutput.contains("[WARNING] Complex expression in 'println'"));
    }

    @Test
    void testFormatterModifiesFile() throws Exception {
        // 1. Obtenemos el archivo original de resources
        Path originalScriptPath = getResourcePath("format_test.ps");
        Path configPath = getResourcePath("formatter_rules.json");

        // 2. Copiamos el script al tempDir para no sobreescribir el código fuente del proyecto
        Path scriptPathToFormat = tempDir.resolve("format_test_copy.ps");
        Files.copy(originalScriptPath, scriptPathToFormat);

        // 3. Ejecutamos el CLI sobre la COPIA temporal
        String[] args = { "formatting", scriptPathToFormat.toString(), "--config", configPath.toString() };
        Main.main(args);

        // 4. Leemos el archivo resultante y verificamos los cambios
        String resultingCode = Files.readString(scriptPathToFormat);

        String expectedCode = "let name: string = \"Joe\";\n\nprintln(name);\n";

        assertEquals(expectedCode.trim(), resultingCode.trim());
        assertTrue(outContent.toString().contains("Formatting successful"));
    }

    @Test
    void testExecutionRunsScriptAndPrintsOutput() throws Exception {
        // 1. Obtenemos la ruta del script de ejecución desde resources
        Path scriptPath = getResourcePath("execution_script.ps");

        // 2. Ejecutamos el CLI en modo "execution"
        String[] args = { "execution", scriptPath.toString() };
        Main.main(args);

        // 3. Verificamos la salida de la consola
        String consoleOutput = outContent.toString();

        // Verificamos los mensajes de la CLI
        assertTrue(consoleOutput.contains("Ejecutando script..."));
        assertTrue(consoleOutput.contains("Execution finished."));

        // Verificamos el output real del intérprete
        assertTrue(consoleOutput.contains("Result: 3"));
    }
}