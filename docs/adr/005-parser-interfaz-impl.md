# ADR-005: Interfaz `Parser` + implementación `ParserImpl`

- **Estado:** Aceptado
- **Fecha:** 2026-08-11

## Contexto

El parser necesita una API pública clara. Las opciones consideradas fueron:

- **Método estático** `Parser.parse(Lexer)`: simple de llamar pero no testeable
  ni mockeable.
- **Instancia** `new ParserImpl(Lexer).parse()`: el parser recibe el lexer en el
  constructor y expone `parse()` como método de instancia.
- **Funcional** `Parser implements Function<Lexer, Program>`: semánticamente
  correcto pero más abstracto y menos idiomático en Java.

## Decisión

Se define la interfaz `Parser` con un único método `parse() → Program`,
implementada por `ParserImpl` que recibe el `Lexer` en el constructor.

```java
public interface Parser {
    Program parse();
}

public class ParserImpl implements Parser {
    public ParserImpl(Lexer lexer) { ... }
    public Program parse() { ... }
}
```

## Consecuencias

- El patrón es consistente con `Lexer` / `LexerImpl` ya existentes en el proyecto.
- `ParserImpl` es fácil de instanciar en tests con un `LexerImpl` real o con un
  lexer stub si fuera necesario.
- El estado interno del parser (campo `current`, buffer de lookahead) queda
  encapsulado en la instancia, sin exponerse en la interfaz.
- Cualquier implementación alternativa del parser (p.ej. para otra versión del
  lenguaje) puede implementar `Parser` sin afectar a los consumidores.
