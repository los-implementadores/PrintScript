# ADR-004: Pratt parsing para expresiones

- **Estado:** Aceptado
- **Fecha:** 2026-08-11

## Contexto

El parser necesita manejar expresiones con precedencia de operadores
(`*` y `/` sobre `+` y `-`) y asociatividad izquierda (`1 + 2 + 3` → `(1 + 2) + 3`).

Las opciones consideradas fueron:

- **Gramática en capas**: una función por nivel de precedencia
  (`parseAddition` llama a `parseMultiplication` que llama a `parsePrimary`).
- **Pratt parsing** (top-down operator precedence): una tabla de binding powers
  y una sola función recursiva `parseExpression(minPower)`.

## Decisión

Se usa Pratt parsing para las expresiones. Cada operador tiene un binding power
numérico. La función `parseExpression(minPower)` incorpora el siguiente operador
solo si su peso supera el mínimo requerido; de lo contrario para y devuelve.

Tabla de binding powers:

| Operador | Peso |
|----------|------|
| `+`      | 10   |
| `-`      | 10   |
| `*`      | 20   |
| `/`      | 20   |

## Consecuencias

- La precedencia queda explícita en una tabla de números, no escondida en la
  estructura de funciones anidadas.
- Agregar un nuevo operador (p.ej. `**`, `%`) requiere solo añadir una entrada
  a la tabla, sin tocar la lógica central del parser.
- El algoritmo es menos intuitivo a primera lectura que la gramática en capas,
  pero queda documentado en `docs/parser.md` con un ejemplo trazado paso a paso.
- La asociatividad izquierda se logra pasando el mismo binding power en la
  llamada recursiva (el operador a la derecha necesita estrictamente mayor peso
  para entrar).
