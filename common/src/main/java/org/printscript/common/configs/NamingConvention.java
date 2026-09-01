package org.printscript.common.configs;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum NamingConvention {
  @JsonProperty("camelCase")
  CAMEL_CASE,

  @JsonProperty("snake_case")
  SNAKE_CASE
}
