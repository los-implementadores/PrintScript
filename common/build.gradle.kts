plugins {
    id("printscript.java-conventions")
}

// common no tiene dependencias externas: es el contrato que comparten todos los módulos
// (tokens, posiciones, AST, Environment) y el módulo más estable del proyecto.
