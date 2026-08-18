plugins {
    id("printscript.java-conventions")
}

dependencies {
    implementation(project(":common"))
    implementation(project(":lexer"))
    implementation(project(":parser"))
    testImplementation(project(":common"))
    testImplementation(project(":lexer"))
    testImplementation(project(":parser"))
}
