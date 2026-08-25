package org.printscript.common.configs;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.Reader;

/**
 * Representa la configuración externa
 */
public class AnalyzerConfig {
    public boolean activeNamingConvention = true;

    public NamingConvention namingConventionFormat = NamingConvention.CAMEL_CASE;

    public boolean activeComplexPrintln = true;

    public boolean activeUnusedVariables = true;

    /**
     * Carga las reglas desde un JSON usando Jackson.
     * Si falla, imprime un error y devuelve la configuración por defecto.
     *
     * @param reader fuente de caracteres del JSON
     * @return las reglas parseadas
     */
    public static AnalyzerConfig fromJson(Reader reader) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(reader, AnalyzerConfig.class);
        } catch (Exception e) {
            System.err.println("Error leyendo la configuración del Linter: " + e.getMessage() + ". Usando reglas por defecto.");
            return new AnalyzerConfig();
        }
    }
}