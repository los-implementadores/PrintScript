plugins {
    id("printscript.application-conventions")
}

dependencies {
    // La CLI sólo conoce la capa de orquestación; lexer/parser/analyzer quedan detrás de `runner`.
    implementation(project(":runner"))
}

application {
    mainClass.set("org.printscript.cli.Main")
}

tasks.named<JavaExec>("run") {
    workingDir = rootProject.projectDir
    // Sin esto Gradle le pasa un stdin vacío al programa, y el menú interactivo y readInput fallan.
    standardInput = System.`in`
}
