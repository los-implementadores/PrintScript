package org.printscript.parser;

import org.printscript.common.Position;
import org.printscript.common.ast.*;
import org.printscript.common.token.Token;
import org.printscript.common.token.TokenType;
import org.printscript.lexer.Lexer;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementación de {@link Parser} usando recursivo descendente para sentencias
 * y Pratt parsing (top-down operator precedence) para expresiones.
 *
 * <h2>Estructura general</h2>
 * <pre>
 *   parse()           → Program
 *   parseStatement()  → Statement
 *     parseVarDeclaration()   → VarDeclarationStatement   (let ...)
 *     parseAssignment()       → AssignmentStatement       (id = ...)
 *     parseExpressionStmt()   → ExpressionStatement       (expr ;)
 *   parseExpression(minPower) → Expression   ← Pratt
 *     parsePrimary()          → Expression   (literal, id, paréntesis, call)
 * </pre>
 *
 * <h2>Tabla de binding powers (Pratt)</h2>
 * <pre>
 *   +  →  10   (left-associative)
 *   -  →  10   (left-associative)
 *   *  →  20   (left-associative)
 *   /  →  20   (left-associative)
 * </pre>
 *
 * <h2>Lookahead</h2>
 * El parser mantiene un campo {@code current} con el token que está por consumir.
 * No se modifica la interfaz {@link Lexer}.
 *
 * <h2>Errores</h2>
 * Estrategia fail-fast: se lanza {@link ParseException} en el primer error.
 */
public class ParserImpl implements Parser {

    // ------------------------------------------------------------------ Pratt
    /** Binding power de cada operador binario infix. */
    private static int bindingPower(TokenType type) {
        switch (type) {
            case PLUS:
            case MINUS:
                return 10;
            case STAR:
            case SLASH:
                return 20;
            default:
                return 0; // no es un operador infix
        }
    }

    // ------------------------------------------------------------------ Estado
    private final Lexer lexer;

    /**
     * Token actual (el que está por ser consumido).
     * Se avanza con {@link #advance()}.
     */
    private Token current;

    // ---------------------------------------------------------------- Constructor
    public ParserImpl(Lexer lexer) {
        this.lexer = lexer;
        this.current = lexer.next(); // carga el primer token
    }

    // ---------------------------------------------------------------- Parser API
    @Override
    public Program parse() {
        List<Statement> statements = new ArrayList<>();
        Position start = current.getPosition();

        while (current.getType() != TokenType.EOF) {
            statements.add(parseStatement());
        }

        Position end = current.getPosition(); // posición del EOF
        return new Program(statements, span(start, end));
    }

    // ---------------------------------------------------------------- Statements

    private Statement parseStatement() {
        if (current.getType() == TokenType.LET) {
            return parseVarDeclaration();
        }

        // IDENTIFIER seguido de ASSIGN → asignación
        if (current.getType() == TokenType.IDENTIFIER) {
            return parseAssignmentOrExpressionStatement();
        }

        // Cualquier otra cosa se intenta parsear como expresión-sentencia
        return parseExpressionStatement();
    }

    /**
     * Parsea {@code let name: type = expr ;}.
     */
    private VarDeclarationStatement parseVarDeclaration() {
        Position start = current.getPosition();
        consume(TokenType.LET);

        Token nameToken = current;
        consume(TokenType.IDENTIFIER);
        Identifier name = new Identifier(nameToken.getLexeme(), nameToken.getPosition());

        consume(TokenType.COLON);

        String typeName = parseTypeName();

        consume(TokenType.ASSIGN);

        Expression initializer = parseExpression(0);

        Token semi = current;
        consume(TokenType.SEMICOLON);

        return new VarDeclarationStatement(name, typeName, initializer, span(start, semi.getPosition()));
    }

    /**
     * Determina si es {@code id = expr ;} o {@code expr ;} mirando el token
     * después del identificador (lookahead de 1 nivel a través del estado interno).
     *
     * <p>Para no consumir el identificador prematuramente, primero lo leemos
     * como expresión primaria y luego chequeamos si sigue un {@code =}.
     */
    private Statement parseAssignmentOrExpressionStatement() {
        Position start = current.getPosition();

        Token nameToken = current;
        advance(); // consume el IDENTIFIER

        if (current.getType() == TokenType.ASSIGN) {
            // Es una asignación
            advance(); // consume el =
            Identifier target = new Identifier(nameToken.getLexeme(), nameToken.getPosition());
            Expression value = parseExpression(0);
            Token semi = current;
            consume(TokenType.SEMICOLON);
            return new AssignmentStatement(target, value, span(start, semi.getPosition()));
        }

        // No es asignación: el identificador era el inicio de una expresión.
        // Construimos el nodo Identifier y continuamos con Pratt desde él.
        Expression left = new Identifier(nameToken.getLexeme(), nameToken.getPosition());
        Expression expr = parseExpressionWithLeft(left, 0);
        Token semi = current;
        consume(TokenType.SEMICOLON);
        return new ExpressionStatement(expr, span(start, semi.getPosition()));
    }

    private ExpressionStatement parseExpressionStatement() {
        Position start = current.getPosition();
        Expression expr = parseExpression(0);
        Token semi = current;
        consume(TokenType.SEMICOLON);
        return new ExpressionStatement(expr, span(start, semi.getPosition()));
    }

    // ---------------------------------------------------------------- Tipo

    private String parseTypeName() {
        if (current.getType() == TokenType.TYPE_NUMBER) {
            String name = current.getLexeme();
            advance();
            return name;
        }
        if (current.getType() == TokenType.TYPE_STRING) {
            String name = current.getLexeme();
            advance();
            return name;
        }
        throw new ParseException(
                "Expected type name ('number' or 'string'), found '" + current.getLexeme() + "'",
                current.getPosition()
        );
    }

    // ---------------------------------------------------------------- Pratt expressions

    /**
     * Punto de entrada del Pratt parser.
     *
     * @param minPower binding power mínimo que debe tener el próximo operador
     *                 para ser incorporado a la expresión actual.
     */
    private Expression parseExpression(int minPower) {
        Expression left = parsePrimary();
        return parseExpressionWithLeft(left, minPower);
    }

    /**
     * Continúa un Pratt parse con un {@code left} ya construido.
     * Permite reutilizar la lógica cuando el identificador ya fue consumido.
     */
    private Expression parseExpressionWithLeft(Expression left, int minPower) {
        while (true) {
            int power = bindingPower(current.getType());
            if (power <= minPower) break;

            Token opToken = current;
            advance(); // consume el operador

            // Asociatividad izquierda: pasamos el mismo power para que el mismo
            // operador a la derecha NO entre (necesita > power).
            Expression right = parseExpression(power);
            left = new BinaryExpression(left, opToken.getLexeme(), right,
                    span(left.getPosition(), right.getPosition()));
        }
        return left;
    }

    /**
     * Parsea un operando primario: literal, identificador, llamada a función
     * o expresión entre paréntesis.
     */
    private Expression parsePrimary() {
        Token token = current;

        switch (token.getType()) {
            case NUMBER_LITERAL: {
                advance();
                double value = Double.parseDouble(token.getLexeme());
                return new NumberLiteral(value, token.getPosition());
            }

            case STRING_LITERAL: {
                advance();
                return new StringLiteral(token.getLexeme(), token.getPosition());
            }

            case IDENTIFIER: {
                advance();
                // ¿Es una llamada a función? id ( ...
                if (current.getType() == TokenType.LPAREN) {
                    return parseCallExpression(token);
                }
                return new Identifier(token.getLexeme(), token.getPosition());
            }

            case LPAREN: {
                advance(); // consume (
                Expression inner = parseExpression(0);
                Token close = current;
                consume(TokenType.RPAREN);
                // La posición abarca los paréntesis
                return wrapPosition(inner, span(token.getPosition(), close.getPosition()));
            }

            default:
                throw new ParseException(
                        "Unexpected token '" + token.getLexeme() + "'",
                        token.getPosition()
                );
        }
    }

    /**
     * Parsea una llamada a función {@code name(arg1, arg2, ...)}.
     * El token del nombre ya fue consumido y se recibe como parámetro.
     */
    private CallExpression parseCallExpression(Token nameToken) {
        consume(TokenType.LPAREN);
        List<Expression> args = new ArrayList<>();

        if (current.getType() != TokenType.RPAREN) {
            args.add(parseExpression(0));
            // En PrintScript 1.0 solo se soporta un argumento, pero la
            // estructura permite extenderlo fácilmente.
        }

        Token close = current;
        consume(TokenType.RPAREN);
        return new CallExpression(nameToken.getLexeme(), args,
                span(nameToken.getPosition(), close.getPosition()));
    }

    // ---------------------------------------------------------------- Helpers

    /**
     * Consume el token actual si su tipo coincide con {@code expected},
     * y avanza al siguiente. Lanza {@link ParseException} si no coincide.
     */
    private void consume(TokenType expected) {
        if (current.getType() != expected) {
            throw new ParseException(
                    "Expected " + expected + " but found '" + current.getLexeme() + "'",
                    current.getPosition()
            );
        }
        advance();
    }

    /** Avanza al siguiente token del lexer. */
    private void advance() {
        if (lexer.hasNext()) {
            current = lexer.next();
        }
        // Si no hay más tokens, current queda en EOF (ya lo estamos mirando)
    }

    /** Construye una {@link Position} que abarca desde {@code start} hasta {@code end}. */
    private Position span(Position start, Position end) {
        return new Position(start.getStartLine(), start.getStartColumn(),
                end.getEndLine(), end.getEndColumn());
    }

    /**
     * Devuelve la expresión con una posición diferente.
     * Se usa para envolver expresiones entre paréntesis con la posición correcta.
     */
    private Expression wrapPosition(Expression expr, Position pos) {
        // Los nodos son inmutables, así que delegamos en el mismo nodo
        // pero lo envolvemos solo si la posición importa para el caller.
        // En la práctica, los paréntesis no generan un nodo propio en el AST —
        // simplemente la posición queda en el nodo interno.
        // Retornamos el inner tal cual; la posición de los paréntesis se pierde
        // intencionalmente (el AST refleja estructura semántica, no sintáctica).
        return expr;
    }
}
