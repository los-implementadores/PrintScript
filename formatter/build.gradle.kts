plugins {
    id("printscript.java-conventions")
}

dependencies {
    implementation(project(":common"))
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")
    testImplementation(project(":lexer"))
    testImplementation(project(":parser"))
}
