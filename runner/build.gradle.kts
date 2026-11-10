plugins {
    id("printscript.java-conventions")
    `java-library`
}

dependencies {
    // Tipos que forman parte de la firma pública de los servicios (configs, providers de I/O,
    // violaciones): se exponen como `api` para que los consumidores (CLI, futura API HTTP) no
    // tengan que redeclararlos.
    api(project(":interpreter"))
    api(project(":formatter"))
    api(project(":analyzer"))
    implementation(project(":source"))
    implementation(project(":token"))
    implementation(project(":ast"))
    implementation(project(":lexer"))
    implementation(project(":parser"))
}
