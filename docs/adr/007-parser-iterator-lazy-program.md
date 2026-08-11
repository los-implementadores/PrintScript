# ADR-007: Parser como `Iterator<Statement>` y `Program` como interfaz lazy

- **Estado:** Aceptado
- **Fecha:** 2026-08-11

## Contexto

El parser originalmente devolvía un `Program` completo (clase `final` con una
lista inmutable). Esto significaba que antes de que cualquier módulo pudiera
hacer algo, el parser tenía que haber procesado todo el archivo fuente y cargado
el árbol entero en memoria.

El lexer ya era un `Iterator<Token>` — producía tokens de a uno, sin cargar el
archivo completo. El parser cancelaba esa ventaja al materializar todo al final.

El objetivo era extender el modelo lazy del lexer al parser: que el lexer, el
parser y el módulo consumidor trabajen en pipeline, con el mínimo de datos en
memoria en cada momento.

## Opciones consideradas

1. **`Parser` sigue devolviendo `Program`** — sin cambios, sin beneficio de memoria.
2. **`Parser extends Iterator<Statement>`**, los clientes manejan el iterator — cada módulo tiene que saber cómo consumirlo, delegando responsabilidad hacia afuera.
3. **`Parser extends Iterator<Statement>` + `LazyProgram` como intermediario** — el parser es lazy, pero los clientes siguen recibiendo `Program` como siempre.

## Decisión

Se adoptó la opción 3:

- `Parser extends Iterator<Statement>`: produce sentencias de a una, sin cargar el árbol completo.
- `Program` se convierte en interfaz con dos métodos:
  - `toList()` — materializa todas las sentencias en una lista inmutable.
  - `stream()` — devuelve un `Iterator<Statement>` lazy.
- `EagerProgram` — implementación con lista inmutable, para tests y casos donde se necesita el árbol completo de inmediato.
- `LazyProgram` — envuelve el parser, drena on-demand. La primera llamada a `toList()` drena y cachea; `stream()` delega en el parser de a uno.

Los módulos consumidores siempre reciben `Program` y eligen:

```java
program.stream();   // lazy — para interpreter y formatter
program.toList();   // eager — para analyzer
```

Ningún módulo sabe si el `Program` es lazy o eager.

## Consecuencias

- El lexer, el parser y el interpreter pueden trabajar en pipeline: en cada momento solo existe en memoria el fragmento que se está procesando.
- Los módulos que necesitan el árbol completo (analyzer) llaman `toList()` y obtienen la lista inmutable. El comportamiento es idéntico al anterior.
- La complejidad del streaming queda encapsulada en `LazyProgram` — no se filtra hacia los módulos consumidores.
- Agregar un módulo nuevo que quiera ser lazy solo requiere usar `program.stream()` en vez de `program.toList()`.
- La única restricción: no mezclar `stream()` y `toList()` sobre el mismo `LazyProgram` antes de que el cache esté construido. Documentado en `LazyProgram`.
