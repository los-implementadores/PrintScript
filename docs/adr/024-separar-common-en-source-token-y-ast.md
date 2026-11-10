# ADR-024: Separar `common` en `source`, `token` y `ast`

- **Estado:** Aceptado
- **Fecha:** 2026-11-10
- **Relacionado:** ADR-001 (AST en `common`, reemplazado por este), ADR-014 (cada módulo es dueño de sus tipos), ADR-025 (versiones solo en el runner)

## Contexto

ADR-001 puso el AST en `common` para que ninguna herramienta dependiera del parser, y ADR-014 sacó
de ahí los tipos del linter. Aun así, `common` seguía juntando cosas que no tienen que ver entre sí:
la posición en el código fuente, los tokens, el AST, la tabla de símbolos del intérprete
(`Environment`) y las versiones del lenguaje (`LanguageVersion`).

Un módulo "común" es un cajón de sastre: no tiene una responsabilidad propia, y cualquier cosa que
"usan varios" termina ahí. El costo se ve en las dependencias. El lexer arrastraba el AST, el
`Environment` y las versiones sin usarlos; el linter, los tokens. Cada cambio en `common` le
llegaba a todos los módulos (viola el Common Reuse Principle: quien usa una clase de un módulo
debería usarlas todas).

## Decisión

`common` desaparece. Cada cosa va al módulo que la necesita o a uno propio con una sola
responsabilidad:

| Antes (`org.printscript.common…`) | Ahora | Por qué |
| --- | --- | --- |
| `Position`, `PrintScriptException` | módulo `source` (`org.printscript.source`) | La ubicación en el código fuente y el error del programa del usuario: los usan los tokens, el AST y las excepciones de cada etapa |
| `token.Token`, `token.TokenType` | módulo `token` (`org.printscript.token`) | El vocabulario que comparten el lexer, el parser y el formatter |
| `ast.*` | módulo `ast` (`org.printscript.ast`) | El árbol que produce el parser y recorren el intérprete y el linter |
| `env.Environment` | `interpreter` (`org.printscript.interpreter.scope`) | Solo lo usan el semántico y el intérprete |
| `LanguageVersion` | `runner` (`org.printscript.runner`) | Solo el orquestador sabe de versiones (ADR-025) |

`token` y `ast` exponen `source` como `api`, porque `Position` es parte de la firma de un token y
de cada nodo. Con esto cada módulo depende solo de lo que usa: el lexer, de `token`; el parser, de
`token` y `ast`; el intérprete y el linter, de `ast`.

## Consecuencias

- **Cambio incompatible para quien usa los paquetes publicados**: cambian los paquetes de los
  tipos de `common`, y el artefacto `org.printscript:common` deja de existir (hay `source`, `token`
  y `ast`). Como la 2.0.0 todavía no se publicó, entra en esa versión.
- Un cambio en el AST ya no recompila el lexer, y uno en los tokens no toca al linter.
- Hay más módulos, pero cada uno se explica en una línea.
