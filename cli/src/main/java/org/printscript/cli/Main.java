package org.printscript.cli;

/**
 * TODO: CLI. Argumentos esperados (ver consigna):
 *   1) operación: Validation | Execution | Formatting | Analyzing
 *   2) archivo fuente
 *   3) versión (opcional, por ahora solo "1.0")
 *   4) argumentos propios de cada operación (ej: archivo de config del formatter)
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Uso: printscript <Validation|Execution|Formatting|Analyzing> <archivo> [version] [opciones]");
            System.exit(1);
        }
        System.out.println("PrintScript CLI - TODO: implementar operación '" + args[0] + "' sobre " + args[1]);
    }
}
