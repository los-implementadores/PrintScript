import org.gradle.api.publish.maven.MavenPublication

plugins {
    java
    checkstyle
    pmd
    id("com.diffplug.spotless")
    `maven-publish`
    jacoco
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()
            from(project.components["java"])
        }
    }
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

checkstyle {
    toolVersion = "10.17.0"
    configFile = rootProject.file("config/checkstyle/checkstyle.xml")
    // Un warning frena el build (y el pre-commit). Si se pudiera ignorar, nadie lo miraría.
    maxWarnings = 0
}

pmd {
    toolVersion = "7.3.0"
    isConsoleOutput = true
    ruleSetFiles = files("${rootProject.projectDir}/config/pmd/pmd-rules.xml")
}

tasks.withType<Pmd> {
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

spotless {
    java {
        googleJavaFormat("1.22.0")
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

repositories {
    mavenCentral()
}

// Fija la versión exacta de cada dependencia, también de las transitivas, en el gradle.lockfile
// de cada módulo. Si una versión cambia sin actualizar el lockfile, el build falla.
// Para actualizarlos: ./gradlew resolveAndLockAll --write-locks
dependencyLocking {
    lockAllConfigurations()
}

tasks.register("resolveAndLockAll") {
    description = "Resuelve todas las dependencias para regenerar los lockfiles (con --write-locks)."
    group = "build setup"
    notCompatibleWithConfigurationCache("Resuelve configuraciones al ejecutarse")
    doFirst {
        require(gradle.startParameter.isWriteDependencyLocks) {
            "Hay que correrlo con --write-locks"
        }
    }
    doLast {
        configurations.filter { it.isCanBeResolved }.forEach { it.resolve() }
    }
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
    finalizedBy(tasks.named("jacocoTestReport"))
}

// Reporte de cobertura de cada módulo en build/reports/jacoco/test/html.
tasks.named<JacocoReport>("jacocoTestReport") {
    dependsOn(tasks.named("test"))
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

// Cualquier compilación deja configurados los git hooks del repo (installGitHooks en el build raíz).
tasks.named("compileJava") {
    dependsOn(":installGitHooks")
}
