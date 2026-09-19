# Integración con el PrintScript TCK (Test Compatibility Kit)

Esta guía explica cómo conectar nuestra implementación de **PrintScript** con el repositorio de validación **printscript-tck**, cómo funciona la publicación con Maven (local y remota con GitHub Packages), y cómo están diseñados los adapters que comunican ambos proyectos.

---

## 1. Arquitectura general

El TCK es un repositorio independiente mantenido por la cátedra que contiene la suite oficial de pruebas de compatibilidad para PrintScript 1.0 y 1.1.

```
┌────────────────────────────────────────────────────────┐
│                      PrintScript                       │
│  (Módulos: common, lexer, parser, interpreter, etc.)   │
└──────────────────────────┬─────────────────────────────┘
                           │  1. ./gradlew publishToMavenLocal
                           │     (o GitHub Packages)
                           ▼
                 [ Repositorio Maven ]
            (~/.m2/ o maven.pkg.github.com)
                           │
                           │  2. implementation 'org.printscript:...'
                           ▼
┌────────────────────────────────────────────────────────┐
│                    printscript-tck                     │
│  ├── CustomImplementationFactory                       │
│  ├── Adapters (Interpreter, Formatter, Linter)         │
│  └── Test Suites (Validation, Statements, LargeFile)   │
└────────────────────────────────────────────────────────┘
```

El TCK **no conoce las clases internas** de nuestro proyecto; solo define interfaces fijas (`PrintScriptInterpreter`, `PrintScriptFormatter`, `PrintScriptLinter`). Para unir ambos mundos:
1. Publicamos nuestros módulos como librerías Maven.
2. El TCK importa esas librerías como dependencias en su `build.gradle`.
3. Implementamos los **Adapters** requeridos en `CustomImplementationFactory`.

---

## 2. Cómo funciona Maven y `maven-publish`

### ¿Qué es un artefacto Maven?
Cuando Gradle compila un módulo (ej. `interpreter`), genera:
* Un archivo `.jar` con los `.class` compilados.
* Un archivo `.pom` con los metadatos: identificador (`groupId:artifactId:version`) y sus dependencias transitivas.

En nuestro proyecto, definimos el grupo y la versión de SemVer en [`gradle.properties`](../gradle.properties):
```properties
group=org.printscript
version=1.1.0-SNAPSHOT
```

Y en [`buildSrc/src/main/kotlin/printscript.java-conventions.gradle.kts`](../buildSrc/src/main/kotlin/printscript.java-conventions.gradle.kts) se configuran dinámicamente:

```kotlin
import org.gradle.api.publish.maven.MavenPublication

plugins {
    `maven-publish`
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = project.name     // common, lexer, parser, interpreter, etc.
            version = project.version.toString()
            from(project.components["java"])
        }
    }
}
```

Al aplicarlo en las convenciones, **todos los submódulos heredan automáticamente la capacidad de publicarse**.

---

## 3. Flujo de Desarrollo Local (Paso a Paso)

Para que cualquier miembro del equipo clone ambos repositorios y corra el TCK en su máquina:

### Paso 1: Publicar PrintScript a Maven Local
En la raíz del proyecto `PrintScript`:
```bash
./gradlew publishToMavenLocal -x test -x checkstyleMain -x pmdMain -x spotlessCheck
```
> Esto compila los `.jar` y los copia a tu carpeta local `~/.m2/repository/org/printscript/` bajo la versión `1.1.0-SNAPSHOT`.

### Paso 2: Configurar dependencias en el TCK
En el repositorio `printscript-tck`, el archivo `build.gradle` ya incluye `repositories { mavenLocal() }`. En el bloque de dependencias se configuran nuestros módulos:

```groovy
dependencies {
    testImplementation 'junit:junit:4.13.1'

    implementation 'org.printscript:common:1.1.0-SNAPSHOT'
    implementation 'org.printscript:lexer:1.1.0-SNAPSHOT'
    implementation 'org.printscript:parser:1.1.0-SNAPSHOT'
    implementation 'org.printscript:interpreter:1.1.0-SNAPSHOT'
    implementation 'org.printscript:formatter:1.1.0-SNAPSHOT'
    implementation 'org.printscript:analyzer:1.1.0-SNAPSHOT'
}
```

### Paso 3: Correr los tests del TCK
En la raíz de `printscript-tck`:
```bash
./gradlew test
```
O para correr únicamente los tests de un componente específico (ej. el Interpreter):
```bash
./gradlew test --tests "interpreter.*"
```

> **Nota importante sobre cambios de código:** Cada vez que se modifica código fuente en `PrintScript`, se debe volver a correr `./gradlew publishToMavenLocal` para actualizar los `.jar` en `~/.m2/` antes de reejecutar el TCK.

---

## 4. Publicación Remota: GitHub Packages (CI/CD)

`mavenLocal()` solo existe en tu máquina local. Para que el CI de GitHub Actions del TCK (y el Pull Request hacia el repositorio de la cátedra) pueda compilar y pasar los tests, los artefactos deben publicarse en **GitHub Packages**.

### 1. Configurar la publicación en `PrintScript`
En [`buildSrc/src/main/kotlin/printscript.java-conventions.gradle.kts`](../buildSrc/src/main/kotlin/printscript.java-conventions.gradle.kts), se agrega el repositorio de destino:

```kotlin
publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/los-implementadores/PrintScript")
            credentials {
                username = System.getenv("GITHUB_ACTOR") ?: project.findProperty("gpr.user") as String?
                password = System.getenv("GITHUB_TOKEN") ?: project.findProperty("gpr.key") as String?
            }
        }
    }
}
```

Para publicar a GitHub Packages:
```bash
./gradlew publish
```

### 2. Consumir desde el TCK en GitHub Actions
En el `build.gradle` del TCK, se descomenta el bloque de repositorio correspondiente:

```groovy
repositories {
    mavenLocal()
    mavenCentral()
    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/los-implementadores/PrintScript")
        credentials {
            username = project.findProperty("gpr.user") ?: System.getenv("GITHUB_ACTOR")
            password = project.findProperty("gpr.key") ?: System.getenv("GITHUB_TOKEN")
        }
    }
}
```

En los workflows de GitHub Actions (`.github/workflows/gradle.yml`), GitHub provee automáticamente la variable `GITHUB_TOKEN` con permisos de lectura de paquetes para la organización.

---

## 5. Arquitectura de los Adapters

El patrón **Adapter** resuelve la incompatibilidad entre las interfaces requeridas por el TCK y el diseño interno de nuestro compilador.

```
Interfaces del TCK                 Adapters                    Motor PrintScript
──────────────────                 ────────                    ─────────────────
PrintScriptInterpreter   ──►  InterpreterAdapter   ──►  LexerImpl + ParserImpl + InterpreterImpl
PrintScriptFormatter     ──►  FormatterAdapter     ──►  LexerImpl + ParserImpl + FormatterImpl
PrintScriptLinter        ──►  LinterAdapter        ──►  LexerImpl + ParserImpl + StaticAnalyzerImpl
```

### Punto de entrada: `CustomImplementationFactory`
El TCK instancia los adapters a través de esta fábrica:
```java
public class CustomImplementationFactory implements PrintScriptFactory {
    @Override
    public PrintScriptInterpreter interpreter() { return new PrintScriptInterpreterAdapter(); }

    @Override
    public PrintScriptFormatter formatter() { return new PrintScriptFormatterAdapter(); }

    @Override
    public PrintScriptLinter linter() { return new PrintScriptLinterAdapter(); }
}
```

### Unificación de código duplicado: `AdapterUtils`
Tanto el Interpreter como el Linter y el Formatter necesitan transformar el `(InputStream src, String version)` en un AST ejecutable (`Program`). Para no repetir la instanciación de matchers, lexer y parser en los 3 adapters, creamos el helper [`AdapterUtils`](file:///home/mrecio/Proyectos/ingsis/printscript-tck/src/main/java/implementation/AdapterUtils.java):

```java
public static Program buildProgram(InputStream src, String version) {
    LanguageVersion langVersion = LanguageVersion.fromLabel(version);
    Reader reader = new InputStreamReader(src, StandardCharsets.UTF_8);
    var lexer = new LexerImpl(reader, buildMatchers());
    var parser = new ParserImpl(lexer, langVersion);
    return new LazyProgram(parser, new Position(1, 1, 1, 1));
}
```

### Detalle de cada Adapter

1. **`PrintScriptInterpreterAdapter`**:
   - Convierte el `InputStream` en un `Program` lazy mediante `AdapterUtils.buildProgram`.
   - Adapta el `InputProvider` del TCK (`name -> value`) a nuestro `org.printscript.interpreter.input.InputProvider`.
   - Redirige la salida de `System.out` hacia el `PrintEmitter` inyectado por el test.
   - Captura y reporta excepciones de sintaxis, semántica o memoria (`OutOfMemoryError`) hacia el `ErrorHandler` del TCK.

2. **`PrintScriptFormatterAdapter`**:
   - Construye el `Program` con `AdapterUtils.buildProgram`.
   - Deserializa las reglas desde el `InputStream config` hacia nuestro `FormattingRules`.
   - Ejecuta `FormatterImpl#format(program)` y escribe el resultado directamente en el `Writer` provisto.

3. **`PrintScriptLinterAdapter`**:
   - Construye el `Program` y obtiene el stream de `Statement`.
   - Parsea `AnalyzerConfig` desde el `InputStream config`.
   - Ejecuta `StaticAnalyzerImpl#analyze(stmt)` y reporta cada `Violation` resultante invocando `handler.reportError(...)`.
