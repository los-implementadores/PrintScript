# ADR-026: Las reglas del formatter y del linter se registran con su clave

- **Estado:** Aceptado
- **Fecha:** 2026-11-10
- **Relacionado:** ADR-012 (formateador streaming), ADR-014 (lectura de la configuración con Jackson), ADR-015 (reglas del linter como estrategias), ADR-027 (formatter y linter sin visitor)

## Contexto

Agregar una regla configurable tocaba varios lugares, distintos en cada herramienta:

- **Linter:** la clase de la regla, un campo en `AnalyzerConfig` con su `@JsonProperty`, y el `if`
  que la arma en `LintRules.from`.
- **Formatter:** un campo en `FormattingRules` con sus alias, dos getters (uno con default para el
  formateador de AST y otro sin default para el streaming), y la lógica en `FormatterVisitor` o en
  `StreamingFormatter`, según cuál la soportara. Una misma clave podía significar cosas distintas:
  `line-breaks-after-println` era "antes del println" en uno y "después" en el otro.

La configuración y las reglas eran cosas separadas que había que mantener sincronizadas a mano, y
leer el JSON estaba duplicado en los dos módulos.

## Decisión

Un mecanismo único, en el módulo `rules`, para pasar de un archivo de configuración a reglas:

- `RuleConfig` lee el JSON (solo JSON estándar, como hasta ahora) y guarda los valores. No sabe qué
  reglas existen.
- `RuleRegistry<R>` dice qué regla arma cada clave. Cada herramienta tiene el suyo, que es **el
  único lugar donde se registran sus reglas**: `LintRules` para el linter y `FormattingRules` para
  el formatter.

```java
RuleRegistry.<FormattingRule>builder()
    .rule("spaceAroundAssign").withBoolean(on -> TokenSpacing.around(Set.of(ASSIGN), on))
    .rule("indentSize", "indent-inside-if").withInt(BlockIndentation::new)
    ...
```

Una regla puede tener alias (la clave propia y la del TCK), y su fábrica decide qué hacer con el
valor: `whenOn` (se aplica con `true`), `withBoolean`, `withInt`, `withText` o `with` (lee otras
claves, como `namingConventionFormat`). Las reglas se aplican en el orden en que se registran, y
`create` arma instancias nuevas cada vez porque pueden guardar estado.

## Consecuencias

- Agregar una regla es escribir su clase y una línea en el registro. Ya se puede activar desde el
  JSON: ni el motor de la herramienta ni la lectura de la configuración cambian.
- Jackson queda solo en `rules`; `formatter` y `analyzer` ya no dependen de él.
- Un valor del tipo equivocado (`"indentSize": "cuatro"`) es un error con el nombre de la clave,
  igual que un JSON mal formado.
- **Cambio incompatible:** `AnalyzerConfig` y `FormattingRules` (la clase de configuración del
  formatter) dejan de existir. Los servicios del runner y las herramientas reciben un `RuleConfig`
  (`RuleConfig.fromJson(reader)`).
