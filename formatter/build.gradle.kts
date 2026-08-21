plugins {
    id("printscript.java-conventions")
}

dependencies {
    implementation(project(":common"))
    implementation("com.google.code.gson:gson:2.11.0")
    testImplementation(project(":common"))
    testImplementation(project(":lexer"))
    testImplementation(project(":parser"))
}
