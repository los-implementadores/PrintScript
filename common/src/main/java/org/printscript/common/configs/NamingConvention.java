package org.printscript.common.configs;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public enum NamingConvention {
  @JsonProperty("camelCase")
  CAMEL_CASE,

  @JsonProperty("snake_case")
  SNAKE_CASE;

  @JsonCreator
  public static NamingConvention fromString(String value) {
    if (value == null) {
      return null;
    }
    String normalized = value.trim().toLowerCase().replace(" ", "_").replace("-", "_");
    if ("camelcase".equals(normalized) || "camel_case".equals(normalized)) {
      return CAMEL_CASE;
    }
    if ("snake_case".equals(normalized)) {
      return SNAKE_CASE;
    }
    throw new IllegalArgumentException("Unknown naming convention: " + value);
  }
}
