plugins {
    application
}

dependencies {
    implementation(project(":common"))
    implementation(project(":lexer"))
    implementation(project(":parser"))
    implementation(project(":interpreter"))
    implementation(project(":formatter"))
    implementation(project(":analyzer"))
}

application {
    mainClass.set("org.printscript.cli.Main")
}
