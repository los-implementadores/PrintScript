package org.printscript.cli;

import org.printscript.common.Position;
import org.printscript.common.ast.LazyProgram;
import org.printscript.common.ast.Program;
import org.printscript.common.ast.Statement;
import org.printscript.interpreter.Interpreter;
import org.printscript.interpreter.InterpreterImpl;
import org.printscript.interpreter.SemanticAnalyzer;
import org.printscript.interpreter.SemanticAnalyzerImpl;
import org.printscript.common.env.Environment;
import org.printscript.lexer.Lexer;
import org.printscript.lexer.LexerImpl;
import org.printscript.parser.Parser;
import org.printscript.parser.ParserImpl;

import java.io.FileReader;
import java.io.Reader;
import java.util.Iterator;

public class Main {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: printscript <operation> <file-path> [version]");
            System.out.println("Operations: Validation, Execution");
            return;
        }

        String operation = args[0];
        String filePath = args[1];

        try (Reader reader = new FileReader(filePath)) {

            Lexer lexer = new LexerImpl(reader);
            Parser parser = new ParserImpl(lexer);

            // Suponiendo que arman el LazyProgram como indica la doc de tu amigo
            // (La posición de inicio es un detalle de implementación de su parser)
            Position startPos = new Position(1, 1, 1, 1);
            Program program = new LazyProgram(parser, startPos);

            Iterator<Statement> statementIterator = program.stream();

            Environment symbolTable = new Environment(); // Solo para tipos
            SemanticAnalyzer analyzer = new SemanticAnalyzerImpl(symbolTable);

            Environment memory = new Environment(); // Para valores reales
            Interpreter interpreter = new InterpreterImpl(memory);

            switch (operation.toLowerCase()) {
                case "validation" -> {
                    System.out.println("Validating file...");
                    while (statementIterator.hasNext()) {
                        Statement stmt = statementIterator.next();
                        analyzer.analyze(stmt);
                    }
                    System.out.println("Validation successful.");
                }
                case "execution" -> {
                    while (statementIterator.hasNext()) {
                        Statement stmt = statementIterator.next();

                        // Falla rápido si hay un error semántico
                        analyzer.analyze(stmt);

                        // Si pasó la validación, lo ejecuta
                        interpreter.execute(stmt);
                    }
                }
                default -> System.err.println("Unsupported operation: " + operation);
            }

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }
}