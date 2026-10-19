package org.printscript.interpreter;

import org.printscript.interpreter.env.EnvProvider;
import org.printscript.interpreter.input.InputProvider;
import org.printscript.interpreter.output.OutputProvider;

/**
 * Canales de I/O de una ejecución: de dónde lee el programa ({@code readInput}, {@code readEnv}) y
 * a dónde escribe ({@code println}).
 *
 * <p>Los elige quien arma el intérprete (consola en la CLI, request/response en una API, memoria en
 * los tests). El intérprete no tiene canales por defecto, así no depende de {@code System.*}.
 */
public record ExecutionIo(InputProvider input, EnvProvider env, OutputProvider output) {}
