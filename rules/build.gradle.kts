plugins {
    id("printscript.java-conventions")
}

// Cómo se pasa de un archivo de configuración (JSON) a reglas: lo usan el formatter y el linter,
// cada uno con su propio registro de reglas. Es el único módulo que lee JSON.
dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")
}
