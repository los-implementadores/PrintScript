# ADR-008: Despacho del parser por parselets (registries) en vez de if/switch

- **Estado:** Aceptado
- **Fecha:** 2026-09-01

## Contexto

`ParserImpl` decidía qué parsear con cadenas de `if` / `switch` sobre `TokenType`:

- `parseStatement()` — cadena de `if` (LET / IDENTIFIER / fallback).
- `parsePrimary()` — `switch` grande (NUMBER_LITERAL, STRING_LITERAL, IDENTIFIER, LPAREN, default).
- `bindingPower()` — `switch` de precedencias de operadores.
- `parseTypeName()` — cadena de `if` (TYPE_NUMBER / TYPE_STRING).

Cada construcción nueva del lenguaje obligaba a **modificar** esos métodos, violando
Open/Closed. Además `ParserImpl` concentraba parseo de sentencias, expresiones, tipos,
llamadas y la gestión del stream de tokens, violando SRP.

## Opciones consideradas

1. **Dejar los if/switch** — simple, pero no extensible; cada feature toca el core.
2. **Gramática declarativa externa (YAML) + motor genérico** — separa gramática de
   código, pero agrega una dependencia (Jackson YAML), mueve errores a runtime y
   requiere builders de AST por regla. Maquinaria desproporcionada para el alcance.
3. **Parselets registrados en tablas (`Map<TokenType, Parselet>`)** — despacho por
   lookup; agregar una construcción = registrar un parselet, sin tocar el core.

## Decisión

Se adoptó la opción 3 (patrón parselet, à la Pratt/Crockford):

- `Map<TokenType, StatementParselet>` para sentencias; `parseStatement()` hace lookup
  con un `ExpressionStatementParselet` como fallback por defecto.
- `Map<TokenType, PrefixParselet>` para expresiones prefijas (literales, identificador,
  agrupación por paréntesis).
- `Map<TokenType, InfixParselet>` para operadores infijos, con el **binding power
  embebido en cada parselet** (reemplaza el `switch` de precedencias por datos).
- `TypeNameParser` valida el tipo contra un `Set<TokenType>` (reemplaza la cadena de `if`).
- `TokenStream` encapsula `current`/`advance`/`consume`/`span` (SRP).
- Los parselets dependen de la interfaz `ParseContext`, no de `ParserImpl` (DIP).
- La gramática se registra en un único lugar, `ParserGrammar.printScript()`.

## Consecuencias

- **OCP en el motor del parser**: agregar un statement, una expresión prefija o un
  operador se hace registrando un parselet en `ParserGrammar`, sin modificar
  `ParserImpl` ni los parselets existentes.
- **SRP**: la gestión de tokens vive en `TokenStream`; cada parselet parsea una sola
  construcción.
- `ParserImpl` baja de ~205 a ~63 líneas de lógica.
- El contrato público (`Parser extends Iterator<Statement>`, parseo lazy) y el
  comportamiento no cambian: `ParserTest` no se modificó y sigue pasando.
- El Pratt parsing (ADR-004) se mantiene; los parselets son su expresión natural.
- Costo: más clases pequeñas y un nivel de indirección. Se acepta a cambio de la
  extensibilidad.

## Fuera de alcance

Agregar construcciones nuevas al lenguaje (`if`, `while`, funciones). Este ADR cubre
solo el rediseño del despacho sobre la gramática ya existente. La limitación de OCP
para **nodos nuevos** del AST (no del parser) se trata en ADR-009.
