plugins {
    id("printscript.java-conventions")
}

// El formatter recorre el AST y toma los espacios del código original (SourceText, en `source`),
// como rustfmt (ADR-029). No depende del lexer ni del parser: los tests arman el AST con los reales.
dependencies {
    implementation(project(":ast"))
    implementation(project(":rules"))
    testImplementation(project(":token"))
    testImplementation(project(":lexer"))
    testImplementation(project(":parser"))
}
