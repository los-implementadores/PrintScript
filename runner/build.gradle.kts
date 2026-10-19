plugins {
    id("printscript.java-conventions")
    `java-library`
}

dependencies {
    // Tipos que forman parte de la firma pública de los servicios (configs, versiones,
    // providers de I/O, violaciones): se exponen como `api` para que los consumidores
    // (CLI, futura API HTTP) no tengan que redeclararlos.
    api(project(":common"))
    api(project(":interpreter"))
    api(project(":formatter"))
    implementation(project(":lexer"))
    implementation(project(":parser"))
    implementation(project(":analyzer"))
}
