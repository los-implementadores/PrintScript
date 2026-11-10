plugins {
    id("printscript.java-conventions")
    `java-library`
}

dependencies {
    // Tipos que forman parte de la firma pública de los servicios (configuración de reglas,
    // providers de I/O, violaciones): se exponen como `api` para que los consumidores (CLI, futura
    // API HTTP) no tengan que redeclararlos.
    api(project(":rules"))
    api(project(":interpreter"))
    api(project(":analyzer"))
    implementation(project(":formatter"))
    implementation(project(":source"))
    implementation(project(":token"))
    implementation(project(":ast"))
    implementation(project(":lexer"))
    implementation(project(":parser"))
}
