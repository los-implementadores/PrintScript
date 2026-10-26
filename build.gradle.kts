// Root project — no aplica plugins propios.
// La configuración común está en buildSrc/src/main/kotlin/printscript.java-conventions.gradle.kts

// Hace que git use los hooks versionados en hooks/ (core.hooksPath). Corre solo antes de cualquier
// compilación (ver printscript.java-conventions), así nadie tiene que acordarse de instalarlos.
// Como git los lee directo de hooks/, un cambio en un hook le llega a todos con el próximo pull.
val installGitHooks by tasks.registering(Exec::class) {
    description = "Configura git para usar los hooks de hooks/ (core.hooksPath)."
    group = "build setup"
    val gitDir = layout.projectDirectory.file(".git").asFile
    val currentHooksPath =
        providers
            .exec {
                commandLine("git", "config", "--get", "core.hooksPath")
                isIgnoreExitValue = true
            }
            .standardOutput
            .asText
            .map { it.trim() }
    onlyIf("es un clon de git") { gitDir.exists() }
    // Si ya hay un core.hooksPath (por ejemplo uno global de un hook manager), no se pisa: los
    // hooks de esa persona dejarían de correr en este repo sin ningún aviso.
    onlyIf("no hay ningún core.hooksPath configurado") {
        val current = currentHooksPath.get()
        if (current.isNotEmpty() && current != "hooks") {
            logger.warn(
                "git ya usa core.hooksPath=$current, así que no se configuran los hooks de hooks/. " +
                    "Para usarlos en este repo: git config core.hooksPath hooks",
            )
        }
        current.isEmpty()
    }
    commandLine("git", "config", "core.hooksPath", "hooks")
}
