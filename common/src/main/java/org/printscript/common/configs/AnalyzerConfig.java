package org.printscript.common.configs;

/**
 * Representa la configuración externa
 */
public class AnalyzerConfig {
    public boolean activeNamingConvention = true;

    public NamingConvention namingConventionFormat = NamingConvention.CAMEL_CASE;

    public boolean activeComplexPrintln = true;

    public boolean activeUnusedVariables = true;
}