plugins {
    id("printscript.java-conventions")
}

dependencies {
    implementation(project(":token"))
    implementation(project(":ast"))
    testImplementation(project(":lexer"))
}
