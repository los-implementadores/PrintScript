package org.printscript.common.configs;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.Reader;

/** Representa la configuración externa */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AnalyzerConfig {

  @JsonProperty("activeNamingConvention")
  public boolean activeNamingConvention = false;

  @JsonProperty("namingConventionFormat")
  public NamingConvention namingConventionFormat = NamingConvention.CAMEL_CASE;

  @JsonProperty("activeComplexPrintln")
  @JsonAlias("mandatory-variable-or-literal-in-println")
  public boolean activeComplexPrintln = false;

  @JsonProperty("activeComplexReadInput")
  @JsonAlias("mandatory-variable-or-literal-in-readInput")
  public boolean activeComplexReadInput = false;

  @JsonProperty("activeUnusedVariables")
  public boolean activeUnusedVariables = false;

  @JsonProperty("identifier_format")
  public void setIdentifierFormat(NamingConvention format) {
    if (format != null) {
      this.activeNamingConvention = true;
      this.namingConventionFormat = format;
    }
  }

  /**
   * Carga las reglas desde un JSON usando Jackson. Si falla, imprime un error y devuelve la
   * configuración por defecto.
   *
   * @param reader fuente de caracteres del JSON
   * @return las reglas parseadas
   */
  public static AnalyzerConfig fromJson(Reader reader) {
    try {
      ObjectMapper mapper = new ObjectMapper();
      return mapper.readValue(reader, AnalyzerConfig.class);
    } catch (Exception e) {
      System.err.println(
          "Error leyendo la configuración del Linter: "
              + e.getMessage()
              + ". Usando reglas por defecto.");
      return new AnalyzerConfig();
    }
  }
}
