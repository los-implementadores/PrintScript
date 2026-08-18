# ADR-004: Strategy pattern para reconocimiento de tokens en el lexer

- **Estado:** Aceptado
- **Fecha:** 2026-08-18

## Contexto

El lexer original usaba condicionales (`if`/`switch`) dentro de `next()` para decidir
qué tipo de token construir según el caracter actual: identificadores, números, strings
y símbolos. Cada nuevo tipo de token requería modificar `LexerImpl`, violando el
Open/Closed Principle (OCP).

En una materia de diseño de software, necesitamos que el lexer sea extensible sin
modificación.

## Decisión

Aplicamos el **Strategy pattern** con la interfaz `TokenMatcher`:

```java
public interface TokenMatcher {
    boolean matches(char currentChar);
    Token extract(LexerContext context);
}
```

El `LexerImpl` recibe una `List<TokenMatcher>` por constructor y en `next()` simplemente
itera hasta que un matcher reconoce el caracter actual:

```java
for (TokenMatcher matcher : matchers) {
    if (matcher.matches(c)) {
        return matcher.extract(this);
    }
}
```

Para que los matchers puedan leer y avanzar caracteres sin conocer la implementación
del lexer, se introduce `LexerContext` — una interfaz que expone `getCurrentChar()`,
`advance()`, posición y `createToken(...)`.

Las implementaciones actuales son:
- `IdentifierTokenMatcher` — identificadores y keywords (recibe `Map<String, TokenType>`)
- `NumberTokenMatcher` — literales numéricos (enteros y decimales)
- `StringTokenMatcher` — literales string delimitados por `"` o `'`
- `SymbolTokenMatcher` — operadores y puntuación (recibe `Map<Character, TokenType>`)

## Consecuencias

- **OCP cumplido:** para agregar un nuevo tipo de token (e.g. comentarios, booleanos),
  se crea una nueva clase `TokenMatcher` y se agrega a la lista, sin tocar `LexerImpl`.
- **SRP cumplido:** cada matcher tiene una sola responsabilidad (reconocer un tipo de token).
- **Testeabilidad:** cada matcher se puede testear unitariamente por separado.
- **Configurabilidad:** el mismo `LexerImpl` sirve para distintas versiones del lenguaje
  solo cambiando la lista de matchers y los mapas de keywords/símbolos.
- **Trade-off:** más clases que antes, pero cada una es simple y cohesiva.
