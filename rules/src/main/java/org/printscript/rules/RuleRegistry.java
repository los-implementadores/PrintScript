package org.printscript.rules;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;

/**
 * Qué regla arma cada clave de la configuración. Cada herramienta (formatter, linter) tiene un
 * registro, y es el único lugar donde se dice qué reglas existen: agregar una regla es escribir su
 * clase y registrarla con su clave. Ni el motor de la herramienta ni la lectura del JSON cambian.
 *
 * <pre>
 *   RuleRegistry&lt;FormattingRule&gt; rules =
 *       RuleRegistry.&lt;FormattingRule&gt;builder()
 *           .rule("mandatory-line-break-after-statement").whenOn(LineBreakAfterStatement::new)
 *           .rule("indentSize").withInt(BlockIndentation::new)
 *           .build();
 * </pre>
 *
 * <p>{@link #create} arma instancias nuevas cada vez, por si una regla guarda estado.
 *
 * @param <R> el tipo de regla de la herramienta
 */
public final class RuleRegistry<R> {

  /** Arma la regla a partir del valor de su clave, o no arma nada si ese valor la apaga. */
  @FunctionalInterface
  public interface RuleFactory<R> {
    Optional<R> create(RuleSetting setting);
  }

  private final List<Entry<R>> entries;

  private RuleRegistry(List<Entry<R>> entries) {
    this.entries = List.copyOf(entries);
  }

  public static <R> Builder<R> builder() {
    return new Builder<>();
  }

  /**
   * Arma las reglas que la configuración activa, en el orden en que se registraron.
   *
   * @throws IllegalArgumentException si una clave tiene un valor que su regla no acepta
   */
  public List<R> create(RuleConfig config) {
    List<R> rules = new ArrayList<>();
    for (Entry<R> entry : entries) {
      entry.create(config).ifPresent(rules::add);
    }
    return rules;
  }

  /** Todas las claves que entiende el registro, en orden. */
  public Set<String> keys() {
    Set<String> keys = new LinkedHashSet<>();
    for (Entry<R> entry : entries) {
      keys.addAll(entry.keys);
    }
    return keys;
  }

  /** Las reglas que existen, con qué valor acepta cada una, en el orden en que se registraron. */
  public List<RuleDefinition> definitions() {
    return entries.stream().map(Entry::definition).toList();
  }

  /** Una regla con sus claves. Si la configuración trae más de una, vale la primera. */
  private record Entry<R>(
      List<String> keys,
      RuleValue value,
      List<RuleDefinition.Parameter> parameters,
      RuleFactory<? extends R> factory) {

    RuleDefinition definition() {
      return new RuleDefinition(keys.get(0), keys.subList(1, keys.size()), value, parameters);
    }

    Optional<R> create(RuleConfig config) {
      for (String key : keys) {
        if (config.has(key)) {
          RuleSetting setting = new RuleSetting(key, config.get(key).orElse(null), config);
          return factory.create(setting).map(rule -> rule);
        }
      }
      return Optional.empty();
    }
  }

  /** Registra las reglas de a una, en el orden en que se van a aplicar. */
  public static final class Builder<R> {

    private final List<Entry<R>> entries = new ArrayList<>();

    private Builder() {}

    /**
     * Empieza a registrar una regla. Las claves son alias: la primera es la propia y las demás,
     * otras formas de llamarla (por ejemplo, la del TCK).
     */
    public Keys rule(String key, String... aliases) {
      List<String> keys = new ArrayList<>();
      keys.add(key);
      keys.addAll(List.of(aliases));
      return new Keys(keys);
    }

    public RuleRegistry<R> build() {
      return new RuleRegistry<>(entries);
    }

    /** Las claves de una regla, esperando cómo se arma. */
    public final class Keys {

      private final List<String> keys;
      private final List<RuleDefinition.Parameter> parameters = new ArrayList<>();

      private Keys(List<String> keys) {
        this.keys = keys;
      }

      /**
       * Otra clave que la regla lee cuando está activa, pero que no la activa por sí sola. Solo
       * describe la regla (ver {@link RuleRegistry#definitions()}): la regla lee el valor de {@link
       * RuleSetting#config()}.
       */
      public Keys parameter(String key, RuleValue value) {
        parameters.add(new RuleDefinition.Parameter(key, value));
        return this;
      }

      /** La regla se aplica si el valor es {@code true}; con {@code false} o {@code null}, no. */
      public Builder<R> whenOn(Supplier<? extends R> rule) {
        return with(
            RuleValue.FLAG,
            setting ->
                !setting.isNull() && setting.asBoolean()
                    ? Optional.of(rule.get())
                    : Optional.empty());
      }

      /** La regla recibe el valor: {@code true} y {@code false} configuran cosas distintas. */
      public Builder<R> withBoolean(Function<Boolean, ? extends R> rule) {
        return with(
            RuleValue.BOOLEAN,
            setting ->
                setting.isNull() ? Optional.empty() : Optional.of(rule.apply(setting.asBoolean())));
      }

      /** La regla recibe un número (una cantidad de espacios, de líneas...). */
      public Builder<R> withInt(IntFunction<? extends R> rule) {
        return with(
            RuleValue.INTEGER,
            setting ->
                setting.isNull() ? Optional.empty() : Optional.of(rule.apply(setting.asInt())));
      }

      /** La regla recibe un texto (un formato, un nombre...). */
      public Builder<R> withText(Function<String, ? extends R> rule) {
        return with(
            RuleValue.TEXT,
            setting ->
                setting.isNull() ? Optional.empty() : Optional.of(rule.apply(setting.asText())));
      }

      /**
       * La regla decide qué hacer con el valor (puede leer otras claves de la configuración).
       *
       * @param value qué valor acepta la clave propia, para describir la regla
       */
      public Builder<R> with(RuleValue value, RuleFactory<? extends R> rule) {
        entries.add(new Entry<>(keys, value, List.copyOf(parameters), rule));
        return Builder.this;
      }
    }
  }
}
