plugins {
    id("printscript.java-conventions")
    `java-library`
}

// El árbol que produce el parser y recorren el intérprete y el linter. No depende del parser ni de
// los tokens. Position es parte de la firma de cada nodo, por eso `source` va como `api`.
dependencies {
    api(project(":source"))
}
