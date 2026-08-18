plugins {
    id("printscript.java-conventions")
}

dependencies {
    implementation(project(":common"))
    testImplementation(project(":common"))
    testImplementation(project(":lexer"))
}
