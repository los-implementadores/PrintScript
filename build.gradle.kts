// Root project. La configuración común de los módulos está en
// buildSrc/src/main/kotlin/printscript.java-conventions.gradle.kts

plugins {
    base
    jacoco
}

repositories {
    mavenCentral()
}

tasks.register<Copy>("installGitHooks") {
    description = "Installs git hooks from hooks/ into .git/hooks/"
    group = "setup"
    from("hooks")
    into(".git/hooks")
    filePermissions {
        unix("rwxr-xr-x")
    }
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
