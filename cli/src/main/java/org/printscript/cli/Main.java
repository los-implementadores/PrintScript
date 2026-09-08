package org.printscript.cli;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import org.printscript.analyzer.StaticAnalyzer;
import org.printscript.analyzer.StaticAnalyzerImpl;
import org.printscript.common.LanguageVersion;
import org.printscript.common.Position;
import org.printscript.common.ast.LazyProgram;
import org.printscript.common.ast.Program;
import org.printscript.common.ast.Statement;
import org.printscript.common.configs.AnalyzerConfig;
import org.printscript.common.env.Environment;
import org.printscript.common.linterViolations.Violation;
import org.printscript.common.token.TokenType;
import org.printscript.formatter.Formatter;
import org.printscript.formatter.FormatterImpl;
import org.printscript.formatter.FormattingRules;
import org.printscript.interpreter.Interpreter;
import org.printscript.interpreter.InterpreterImpl;
import org.printscript.interpreter.SemanticAnalyzer;
import org.printscript.interpreter.SemanticAnalyzerImpl;
import org.printscript.lexer.IdentifierTokenMatcher;
import org.printscript.lexer.Lexer;
import org.printscript.lexer.LexerImpl;
import org.printscript.lexer.NumberTokenMatcher;
import org.printscript.lexer.StringTokenMatcher;
import org.printscript.lexer.SymbolTokenMatcher;
import org.printscript.lexer.TokenMatcher;
import org.printscript.parser.Parser;
import org.printscript.parser.ParserImpl;

public class Main {

  public static void main(String[] args) {
    if (args.length >= 2) {
      String operation = args[0];
      String filePath = args[1];
      String configPath = null;
      String versionLabel = "1.0";

      for (int i = 2; i < args.length - 1; i++) {
        if (args[i].equals("--config")) {
          configPath = args[i + 1];
        } else if (args[i].equals("--version")) {
          versionLabel = args[i + 1];
        }
      }

      ejecutarModo(operation, filePath, configPath, versionLabel);
    } else {
      iniciarModoInteractivo();
    }
  }

  private static void iniciarModoInteractivo() {
    Scanner scanner = new Scanner(System.in);
    System.out.println("==========================================");
    System.out.println("      Bienvenido a PrintScript CLI        ");
    System.out.println("==========================================");
    System.out.println("Por favor, seleccione un modo de operacion:");
    System.out.println(" 1. Validation");
    System.out.println(" 2. Analyzing");
    System.out.println(" 3. Execution");
    System.out.println(" 4. Formatting");
    System.out.println(" 0. Salir");
    System.out.print("\nIngrese el numero de la opcion: ");

    String opcion = scanner.nextLine().trim();

    if (opcion.equals("0")) {
      System.out.println("Saliendo...");
      return;
    }

    String operation =
        switch (opcion) {
          case "1" -> "validation";
          case "2" -> "analyzing";
          case "3" -> "execution";
          case "4" -> "formatting";
          default -> null;
        };

    if (operation == null) {
      System.out.println("Opcion invalida. Saliendo...");
      return;
    }

    System.out.print("Ingrese la ruta del script (ej: script.ps): ");
    String filePath = scanner.nextLine().trim();

    String configPath = null;
    if (operation.equals("formatting") || operation.equals("analyzing")) {
      System.out.print("Ingrese la ruta del JSON de configuracion (opcional, Enter para omitir): ");
      String inputConfig = scanner.nextLine().trim();
      if (!inputConfig.isEmpty()) {
        configPath = inputConfig;
      }
    }

    System.out.print("Ingrese la version del lenguaje (1.0 / 1.1) [default 1.0]: ");
    String inputVersion = scanner.nextLine().trim();
    String versionLabel = inputVersion.isEmpty() ? "1.0" : inputVersion;

    // Ejecutamos el motor con los datos ingresados
    ejecutarModo(operation, filePath, configPath, versionLabel);
  }

  private static void ejecutarModo(
      String operation, String filePath, String configPath, String versionLabel) {
    final LanguageVersion version;
    try {
      version = LanguageVersion.fromLabel(versionLabel);
    } catch (IllegalArgumentException e) {
      System.err.println("\n[ERROR] " + e.getMessage());
      System.exit(1);
      return;
    }

    try (Reader reader = new FileReader(filePath)) {

      List<TokenMatcher> matchers =
          List.of(
              new IdentifierTokenMatcher(
                  Map.ofEntries(
                      Map.entry("let", TokenType.LET),
                      Map.entry("const", TokenType.CONST),
                      Map.entry("if", TokenType.IF),
                      Map.entry("else", TokenType.ELSE),
                      Map.entry("number", TokenType.TYPE_NUMBER),
                      Map.entry("string", TokenType.TYPE_STRING),
                      Map.entry("boolean", TokenType.TYPE_BOOLEAN),
                      Map.entry("true", TokenType.BOOLEAN_LITERAL),
                      Map.entry("false", TokenType.BOOLEAN_LITERAL))),
              new NumberTokenMatcher(),
              new StringTokenMatcher(),
              new SymbolTokenMatcher(
                  Map.ofEntries(
                      Map.entry(':', TokenType.COLON),
                      Map.entry('=', TokenType.ASSIGN),
                      Map.entry(';', TokenType.SEMICOLON),
                      Map.entry('(', TokenType.LPAREN),
                      Map.entry(')', TokenType.RPAREN),
                      Map.entry('{', TokenType.LBRACE),
                      Map.entry('}', TokenType.RBRACE),
                      Map.entry('+', TokenType.PLUS),
                      Map.entry('-', TokenType.MINUS),
                      Map.entry('*', TokenType.STAR),
                      Map.entry('/', TokenType.SLASH))));

      Lexer lexer = new LexerImpl(reader, matchers);

      // 2. Configuracion del Parser
      Parser parser = new ParserImpl(lexer, version);

      Position startPos = new Position(1, 1, 1, 1);
      Program program = new LazyProgram(parser, startPos);
      Iterator<Statement> statementIterator = program.stream();

      // 3. Entornos e instancias (Tabla de simbolos y Memoria)
      Environment symbolTable = new Environment(); // Solo para tipos
      SemanticAnalyzer analyzer = new SemanticAnalyzerImpl(symbolTable, version);

      Environment memory = new Environment(); // Para valores reales
      Interpreter interpreter =
          new InterpreterImpl(
              memory, new org.printscript.interpreter.StdinInputProvider(), version);

      // 4. Enrutamiento del modo
      switch (operation.toLowerCase()) {
        case "validation" -> {
          System.out.println("Validando sintaxis del archivo...");
          while (statementIterator.hasNext()) {
            statementIterator.next();
          }
          System.out.println("Validation successful. (Sin errores de sintaxis)");
        }

        case "analyzing" -> {
          System.out.println("Analizando semantica y reglas estaticas (Linter)...");

          AnalyzerConfig config = new AnalyzerConfig();
          if (configPath != null) {
            try (Reader configReader = new FileReader(configPath)) {
              config = AnalyzerConfig.fromJson(configReader);
            }
          }
          StaticAnalyzer staticAnalyzer = new StaticAnalyzerImpl(config);

          while (statementIterator.hasNext()) {
            Statement stmt = statementIterator.next();
            analyzer.analyze(stmt); // 1. Chequeo semántico estricto
            staticAnalyzer.analyze(stmt); // 2. Linter de código estático
          }

          List<Violation> violations = staticAnalyzer.getViolations();
          if (violations.isEmpty()) {
            System.out.println(
                "Analyzing successful. (Tipos correctos y cero violaciones del Linter)");
          } else {
            System.out.println("Linter found violations:");
            for (Violation v : violations) {
              System.out.println(v.toString());
            }
          }
        }

        case "execution" -> {
          System.out.println("Ejecutando script...\n");
          while (statementIterator.hasNext()) {
            Statement stmt = statementIterator.next();
            analyzer.analyze(stmt); // 1. Valida tipos
            interpreter.execute(stmt); // 2. Ejecuta logica
          }
          System.out.println("\nExecution finished.");
        }

        case "formatting" -> {
          System.out.println("Formateando archivo...");

          FormattingRules rules = new FormattingRules();
          if (configPath != null) {
            try (Reader configReader = new FileReader(configPath)) {
              rules = FormattingRules.fromJson(configReader);
            }
          }

          Formatter formatter = new FormatterImpl(rules);
          String formattedCode = formatter.format(program);

          // Sobreescribe el archivo original para cumplir con la CLI[cite: 1]
          try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(formattedCode);
          }
          System.out.println("Formatting successful. El archivo ha sido formateado.");
        }

        default -> System.err.println("Unsupported operation: " + operation);
      }

    } catch (Exception e) {
      System.err.println("\n[ERROR TERMINAL] " + e.getMessage());
      System.exit(1);
    }
  }
}
