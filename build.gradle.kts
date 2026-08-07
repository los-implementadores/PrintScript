// Configuración común a todos los módulos (submódulos declarados en settings.gradle.kts)
subprojects {
    apply(plugin = "java")

    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    }

    repositories {
        mavenCentral()
    }

    dependencies {
        val junitVersion = "5.10.2"
        add("testImplementation", "org.junit.jupiter:junit-jupiter:$junitVersion")
        add("testRuntimeOnly", "org.junit.platform:junit-platform-launcher")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}
