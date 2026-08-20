// Root project — no aplica plugins propios.
// La configuración común está en buildSrc/src/main/kotlin/printscript.java-conventions.gradle.kts

tasks.register<Copy>("installGitHooks") {
    description = "Installs git hooks from hooks/ into .git/hooks/"
    group = "setup"
    from("hooks")
    into(".git/hooks")
    filePermissions {
        unix("rwxr-xr-x")
    }
}

