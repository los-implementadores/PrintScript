plugins {
    id("printscript.java-conventions")
    `java-library`
}

// Vocabulario que comparten el lexer (que produce tokens), el parser y el formatter (que los
// consumen). Position es parte de la firma de Token, por eso `source` va como `api`.
dependencies {
    api(project(":source"))
}
