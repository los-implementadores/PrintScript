package org.printscript.runner;

import org.printscript.interpreter.env.EnvProvider;
import org.printscript.interpreter.input.InputProvider;
import org.printscript.interpreter.output.OutputProvider;

/**
 * Canales de I/O de una ejecución. Quien invoca decide de dónde sale la entrada y a dónde va la
 * salida (consola en la CLI, request/response en una API, memoria en tests).
 */
public record ExecutionIo(InputProvider input, EnvProvider env, OutputProvider output) {}
