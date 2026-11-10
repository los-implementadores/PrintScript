package org.printscript.rules;

import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * La configuración de un conjunto de reglas: cada clave activa o configura una regla.
 *
 * <p>No sabe qué reglas existen: solo guarda los valores. Qué regla arma cada clave lo dice el
 * {@link RuleRegistry} de cada herramienta, y las claves que ningún registro conoce se ignoran.
 */
public final class RuleConfig {

  /**
   * Solo JSON estándar (sin comentarios, comillas simples ni claves sin comillas), rechaza
   * contenido de más después del JSON y no cierra el {@code Reader}: es de quien lo abrió.
   */
  private static final ObjectMapper MAPPER =
      JsonMapper.builder()
          .disable(StreamReadFeature.AUTO_CLOSE_SOURCE)
          .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
          .build();

  private static final TypeReference<LinkedHashMap<String, Object>> OBJECT =
      new TypeReference<>() {};

  private static final RuleConfig EMPTY = new RuleConfig(Map.of());

  private final Map<String, Object> values;

  private RuleConfig(Map<String, Object> values) {
    this.values = Collections.unmodifiableMap(new LinkedHashMap<>(values));
  }

  /** Configuración sin ninguna clave: no activa ninguna regla. */
  public static RuleConfig empty() {
    return EMPTY;
  }

  /**
   * Configuración con los valores dados, como los traería el JSON ({@code Boolean}, números, {@code
   * String} o {@code null}).
   */
  public static RuleConfig of(Map<String, ?> values) {
    return new RuleConfig(new LinkedHashMap<>(values));
  }

  /**
   * Lee la configuración de un objeto JSON.
   *
   * @throws IllegalArgumentException si no es un objeto JSON válido
   */
  public static RuleConfig fromJson(Reader reader) {
    try {
      Map<String, Object> values = MAPPER.readValue(reader, OBJECT);
      if (values == null) {
        throw new IllegalArgumentException(
            "Configuración de reglas inválida: se esperaba un objeto");
      }
      return new RuleConfig(values);
    } catch (IOException e) {
      throw new IllegalArgumentException("Configuración de reglas inválida: " + e.getMessage(), e);
    }
  }

  /** {@code true} si la configuración trae la clave, aunque su valor sea {@code null}. */
  public boolean has(String key) {
    return values.containsKey(key);
  }

  /** El valor de la clave, si está y no es {@code null}. */
  public Optional<Object> get(String key) {
    return Optional.ofNullable(values.get(key));
  }

  /**
   * El valor de una clave de texto, o {@code fallback} si no está o es {@code null}. Sirve para las
   * claves que acompañan a otra (ej. el formato de una regla que se activa con otra clave).
   *
   * @throws IllegalArgumentException si el valor no es un texto
   */
  public String text(String key, String fallback) {
    return get(key).map(value -> new RuleSetting(key, value, this).asText()).orElse(fallback);
  }
}
