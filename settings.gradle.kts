plugins {
    // Si en la máquina no está la JDK que pide el toolchain (17), Gradle la descarga sola.
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "printscript"

include(
    "common",
    "lexer",
    "parser",
    "interpreter",
    "formatter",
    "analyzer",
    "runner",
    "cli"
)
