plugins {
    id("printscript.java-conventions")
}

// El formatter trabaja sobre tokens (con los espacios originales), no sobre el AST: no depende ni
// del parser ni de los nodos. Los tests arman los tokens con el lexer real.
dependencies {
    implementation(project(":token"))
    implementation(project(":rules"))
    testImplementation(project(":lexer"))
}
