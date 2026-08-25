plugins {
    id("printscript.application-conventions")
}

dependencies {
    implementation(project(":common"))
    implementation(project(":lexer"))
    implementation(project(":parser"))
    implementation(project(":interpreter"))
    implementation(project(":formatter"))
    implementation(project(":analyzer"))
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")
}

application {
    mainClass.set("org.printscript.cli.Main")
}

tasks.named<JavaExec>("run") {
    workingDir = rootProject.projectDir
}
