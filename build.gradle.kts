// Root project. La configuración común de los módulos está en
// buildSrc/src/main/kotlin/printscript.java-conventions.gradle.kts

plugins {
    base
    jacoco
}

repositories {
    mavenCentral()
}

// Hace que git use los hooks versionados en hooks/ (core.hooksPath). Corre solo antes de cualquier
// compilación (ver printscript.java-conventions), así nadie tiene que acordarse de instalarlos.
// Como git los lee directo de hooks/, un cambio en un hook le llega a todos con el próximo pull.
val installGitHooks by tasks.registering(Exec::class) {
    description = "Configura git para usar los hooks de hooks/ (core.hooksPath)."
    group = "build setup"
    val gitDir = layout.projectDirectory.file(".git").asFile
    val currentHooksPath =
        providers
            .exec {
                commandLine("git", "config", "--get", "core.hooksPath")
                isIgnoreExitValue = true
            }
            .standardOutput
            .asText
            .map { it.trim() }
    onlyIf("es un clon de git") { gitDir.exists() }
    // Si ya hay un core.hooksPath (por ejemplo uno global de un hook manager), no se pisa: los
    // hooks de esa persona dejarían de correr en este repo sin ningún aviso.
    onlyIf("no hay ningún core.hooksPath configurado") {
        val current = currentHooksPath.get()
        if (current.isNotEmpty() && current != "hooks") {
            logger.warn(
                "git ya usa core.hooksPath=$current, así que no se configuran los hooks de hooks/. " +
                    "Para usarlos en este repo: git config core.hooksPath hooks",
            )
        }
        current.isEmpty()
    }
    commandLine("git", "config", "core.hooksPath", "hooks")
}

// Cobertura de todo el proyecto: cuenta los tests de cualquier módulo (las clases de common, por
// ejemplo, las ejercitan sobre todo los tests del parser y del intérprete).
val modules = subprojects.map { it.name }
val testTasks = modules.map { ":$it:test" }
val coverageExecutionData = files(modules.map { "$it/build/jacoco/test.exec" })
val coverageClasses = files(modules.map { "$it/build/classes/java/main" })
val coverageSources = files(modules.map { "$it/src/main/java" })

val coverageReport by tasks.registering(JacocoReport::class) {
    description = "Reporte de cobertura de todos los módulos (build/reports/jacoco/coverageReport)."
    group = "verification"
    dependsOn(testTasks)
    executionData(coverageExecutionData)
    classDirectories.setFrom(coverageClasses)
    sourceDirectories.setFrom(coverageSources)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

// Un piso de cobertura sólo sirve si frena el build: si baja del mínimo, `check` falla. El mínimo es
// la cobertura que había al agregarlo; se sube cuando la cobertura sube, nunca se baja.
val coverageVerification by tasks.registering(JacocoCoverageVerification::class) {
    description = "Falla si la cobertura de líneas de todo el proyecto baja del mínimo."
    group = "verification"
    dependsOn(testTasks)
    executionData(coverageExecutionData)
    classDirectories.setFrom(coverageClasses)
    sourceDirectories.setFrom(coverageSources)
    violationRules {
        rule {
            limit {
                counter = "LINE"
                minimum = "0.90".toBigDecimal()
            }
            limit {
                counter = "BRANCH"
                minimum = "0.75".toBigDecimal()
            }
        }
    }
}

tasks.named("check") {
    dependsOn(coverageReport, coverageVerification)
}
