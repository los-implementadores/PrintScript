package org.printscript.parser;

import org.printscript.common.Position;
import org.printscript.common.ast.*;
import org.printscript.common.token.Token;
import org.printscript.common.token.TokenType;
import org.printscript.lexer.Lexer;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Implementación de {@link Parser} usando recursivo descendente para sentencias
 * y Pratt parsing (top-down operator precedence) para expresiones.
 *
 * <p>Implementa {@link java.util.Iterator}{@code <Statement>}: produce una sentencia
 * por llamada a {@link #next()}, sin materializar el árbol completo. El lexer
 * subyacente también es lazy, por lo que en cada momento solo existe en memoria
 * el fragmento de código que se está procesando ahora.
 *
 * <h2>Estructura general</h2>
 * <pre>
 *   hasNext()         → true mientras el token actual no sea EOF
 *   next()            → parseStatement()
 *   parseStatement()  → Statement
 *     parseVarDeclaration()              → VarDeclarationStatement   (let ...)
 *     parseAssignmentOrExprStmt()        → AssignmentStatement       (id = ...)
 *                                        → ExpressionStatement       (id(...) o expr ;)
 *     parseExpressionStatement()         → ExpressionStatement       (expr ;)
 *   parseExpression(minPower)            → Expression   ← Pratt
 *     parsePrimary()                     → Expression   (literal, id, paréntesis, call)
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
 * Estrategia fail-fast: {@link #next()} lanza {@link ParseException} en el primer
 * error sintáctico con la {@link Position} del token problemático.
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
                return 0;
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

    // ---------------------------------------------------------------- Iterator API

    /**
     * Devuelve {@code true} mientras haya sentencias por parsear.
     * Es {@code false} cuando el token actual es EOF.
     */
    @Override
    public boolean hasNext() {
        return current.getType() != TokenType.EOF;
    }

    /**
     * Parsea y devuelve la siguiente sentencia.
     *
     * @throws NoSuchElementException si no hay más sentencias (EOF)
     * @throws ParseException         si el código fuente tiene un error sintáctico
     */
    @Override
    public Statement next() {
        if (!hasNext()) {
            throw new NoSuchElementException("No more statements");
        }
        return parseStatement();
    }

    // ---------------------------------------------------------------- Statements

    private Statement parseStatement() {
        if (current.getType() == TokenType.LET) {
            return parseVarDeclaration();
        }
        if (current.getType() == TokenType.IDENTIFIER) {
            return parseAssignmentOrExpressionStatement();
        }
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
     * Distingue entre tres casos que empiezan con un identificador:
     * <ul>
     *   <li>{@code id = expr ;} → asignación</li>
     *   <li>{@code id( ... ) ;} → llamada a función como sentencia</li>
     *   <li>{@code id op expr ;} → expresión binaria como sentencia</li>
     * </ul>
     */
    private Statement parseAssignmentOrExpressionStatement() {
        Position start = current.getPosition();

        Token nameToken = current;
        advance(); // consume el IDENTIFIER

        if (current.getType() == TokenType.ASSIGN) {
            // id = expr ;
            advance(); // consume el =
            Identifier target = new Identifier(nameToken.getLexeme(), nameToken.getPosition());
            Expression value = parseExpression(0);
            Token semi = current;
            consume(TokenType.SEMICOLON);
            return new AssignmentStatement(target, value, span(start, semi.getPosition()));
        }

        if (current.getType() == TokenType.LPAREN) {
            // id( ... ) ;
            CallExpression call = parseCallExpression(nameToken);
            Token semi = current;
            consume(TokenType.SEMICOLON);
            return new ExpressionStatement(call, span(start, semi.getPosition()));
        }

        // id op expr ; — el identificador era el inicio de una expresión binaria
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
            // operador a la derecha NO entre (necesita estrictamente > power).
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
                if (current.getType() == TokenType.LPAREN) {
                    return parseCallExpression(token);
                }
                return new Identifier(token.getLexeme(), token.getPosition());
            }

            case LPAREN: {
                advance(); // consume (
                Expression inner = parseExpression(0);
                consume(TokenType.RPAREN);
                return inner;
            }

            default:
                throw new ParseException(
                        "Unexpected token '" + token.getLexeme() + "'",
                        token.getPosition()
                );
        }
    }

    /**
     * Parsea una llamada a función {@code name(arg1, ...)}.
     * El token del nombre ya fue consumido y se recibe como parámetro.
     */
    private CallExpression parseCallExpression(Token nameToken) {
        consume(TokenType.LPAREN);
        List<Expression> args = new ArrayList<>();

        if (current.getType() != TokenType.RPAREN) {
            args.add(parseExpression(0));
        }

        Token close = current;
        consume(TokenType.RPAREN);
        return new CallExpression(nameToken.getLexeme(), args,
                span(nameToken.getPosition(), close.getPosition()));
    }

    // ---------------------------------------------------------------- Helpers

    private void consume(TokenType expected) {
        if (current.getType() != expected) {
            throw new ParseException(
                    "Expected " + expected + " but found '" + current.getLexeme() + "'",
                    current.getPosition()
            );
        }
        advance();
    }

    private void advance() {
        if (lexer.hasNext()) {
            current = lexer.next();
        }
    }

    private Position span(Position start, Position end) {
        return new Position(start.getStartLine(), start.getStartColumn(),
                end.getEndLine(), end.getEndColumn());
    }
}
