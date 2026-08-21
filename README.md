# PrintScript

TP de Ingeniería de Sistemas 2026 — lexer, parser, interpreter, formatter y
static code analyzer para el lenguaje PrintScript (subset de TypeScript).

## Módulos

| Módulo | Responsabilidad |
|--------|-----------------|
| `common` | Tipos compartidos: `Token`, `TokenType`, `Position`, nodos del AST, `Environment` |
| `lexer` | Análisis léxico. Convierte código fuente en un stream de tokens (Strategy pattern) |
| `parser` | Análisis sintáctico. Convierte tokens en AST usando Pratt parsing |
| `interpreter` | Recorre el AST y ejecuta el programa (incluye análisis semántico) |
| `formatter` | Reformatea código fuente según reglas configurables (JSON) |
| `analyzer` | Static code analyzer (pendiente) |
| `cli` | Punto de entrada. Orquesta los módulos según la operación pedida |

Cada módulo depende solo de lo que necesita (ver `build.gradle.kts` de cada uno).

## Cómo correr

```bash
./gradlew build          # Compila y corre todos los tests
./gradlew test           # Solo tests
./gradlew spotlessApply  # Auto-formatear código (google-java-format)
./gradlew spotlessCheck  # Verificar formato
./gradlew checkstyleMain # Verificar estilo
./gradlew pmdMain        # Análisis estático
./gradlew installGitHooks # Instalar pre-commit y pre-push hooks
```

### CLI

```bash
./gradlew :cli:run --args="Execution script.ps"
./gradlew :cli:run --args="Validation script.ps"
```

## Documentación

- [`docs/ast.md`](docs/ast.md) — Estructura del AST y patrón Visitor
- [`docs/parser.md`](docs/parser.md) — Cómo funciona el parser (Pratt parsing)
- [`docs/formatter.md`](docs/formatter.md) — Formatter y reglas configurables
- [`docs/adr/`](docs/adr/) — Architecture Decision Records

## Tooling

- **Spotless** + google-java-format — formateo automático
- **Checkstyle** — estilo de código (Google Java Style adaptado)
- **PMD** — análisis estático
- **Git hooks** — pre-commit (formato + estilo + PMD), pre-push (tests)

## Estado

- [x] Estructura del proyecto
- [x] Lexer (Strategy pattern con TokenMatcher)
- [x] Parser + AST (Pratt parsing, LazyProgram)
- [x] Interpreter + Análisis semántico
- [x] Formatter (reglas configurables desde JSON)
- [x] CLI (Execution y Validation)
- [x] Tooling (Spotless, Checkstyle, PMD, Git hooks)
- [ ] Static analyzer
