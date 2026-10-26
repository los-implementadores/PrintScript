# ADR-015: Reglas del linter como estrategias registradas

- **Estado:** Aceptado
- **Fecha:** 2026-10-19
- **Relacionado:** ADR-008 (parselets), ADR-009 (limitación OCP del Visitor), ADR-013 (Strategy en el lexer)

## Contexto

El lexer se extiende agregando un `TokenMatcher` (ADR-013) y el parser registrando un parselet
(ADR-008). El linter era la excepción: cada regla era un `if` dentro del `visitX` del nodo que
mira, y el estado de "variables sin usar" estaba mezclado con el recorrido. Agregar una regla
obligaba a modificar `AnalyzerVisitor`.

Opciones consideradas:

1. **Un visitor por regla.** Cada regla recorre el árbol por su cuenta. Es simple, pero repite el
   recorrido en cada regla y lo recorre N veces.
2. **Un recorrido y reglas que reciben eventos.** El visitor recorre una vez y le avisa a cada
   regla los nodos que le interesan.
3. **Dejarlo como estaba.**

## Decisión

Opción 2. `LintRule` es una interfaz con un método por evento (`onDeclaration`, `onAssignment`,
`onCall`, `onIdentifier` y `onFinish`), todos con implementación vacía, así cada regla
sobreescribe solo lo que mira. `AnalyzerVisitor` recorre el AST y llama a las reglas; no conoce
ninguna en particular.

Hay una clase por regla (`NamingConventionRule`, `SimpleArgumentRule`, `UnusedVariableRule`) y
`LintRules.from(config)` es el único lugar donde se registran, igual que `ParserGrammar` para los
parselets. `StaticAnalyzerImpl` también acepta una fábrica de reglas (`Supplier<List<LintRule>>`),
así se puede sumar una regla propia sin tocar nada. Es una fábrica y no una lista porque las reglas
guardan estado: si dos análisis compartieran las mismas instancias, el segundo reportaría
variables del primero.

`onFinish` se llama cada vez que se pide el reporte y no cambia el estado de la regla, así que
`getViolations()` se puede llamar más de una vez sin duplicar resultados.

## Consecuencias

- Agregar una regla es una clase nueva más una línea en `LintRules` (y su clave en
  `AnalyzerConfig` si se activa desde el JSON). El recorrido no se modifica.
- Cada regla guarda su propio estado y se puede testear sola.
- El límite de ADR-009 sigue valiendo: un nodo nuevo en el AST obliga a agregar su `visitX` en
  `AnalyzerVisitor` (y, si alguna regla lo necesita, un evento nuevo en `LintRule`).
