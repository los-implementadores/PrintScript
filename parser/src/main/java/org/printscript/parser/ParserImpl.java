package org.printscript.parser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import org.printscript.common.Position;
import org.printscript.common.ast.*;
import org.printscript.common.token.Token;
import org.printscript.common.token.TokenType;

/**
 * Implementación de {@link Parser} usando recursivo descendente para sentencias y Pratt parsing
 * (top-down operator precedence) para expresiones.
 *
 * <p>Recibe un {@code Iterator<Token>} — no conoce al lexer ni depende de él. El orquestador (CLI)
 * es responsable de crear el lexer y pasar el iterador.
 *
 * <p>Implementa {@link Iterator}{@code <Statement>}: produce una sentencia por llamada a {@link
 * #next()}, sin materializar el árbol completo.
 */
public class ParserImpl implements Parser {

  // ------------------------------------------------------------------ Pratt
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
  private final Iterator<Token> tokens;
  private Token current;

  // ---------------------------------------------------------------- Constructor
  public ParserImpl(Iterator<Token> tokens) {
    this.tokens = tokens;
    this.current = tokens.next(); // carga el primer token
  }

  // ---------------------------------------------------------------- Iterator API

  @Override
  public boolean hasNext() {
    return current.getType() != TokenType.EOF;
  }

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

    return new VarDeclarationStatement(
        name, typeName, initializer, span(start, semi.getPosition()));
  }

  private Statement parseAssignmentOrExpressionStatement() {
    Position start = current.getPosition();

    Token nameToken = current;
    advance();

    if (current.getType() == TokenType.ASSIGN) {
      advance();
      Identifier target = new Identifier(nameToken.getLexeme(), nameToken.getPosition());
      Expression value = parseExpression(0);
      Token semi = current;
      consume(TokenType.SEMICOLON);
      return new AssignmentStatement(target, value, span(start, semi.getPosition()));
    }

    if (current.getType() == TokenType.LPAREN) {
      CallExpression call = parseCallExpression(nameToken);
      Token semi = current;
      consume(TokenType.SEMICOLON);
      return new ExpressionStatement(call, span(start, semi.getPosition()));
    }

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
        current.getPosition());
  }

  // ---------------------------------------------------------------- Pratt expressions

  private Expression parseExpression(int minPower) {
    Expression left = parsePrimary();
    return parseExpressionWithLeft(left, minPower);
  }

  private Expression parseExpressionWithLeft(Expression left, int minPower) {
    Expression result = left;
    while (true) {
      int power = bindingPower(current.getType());
      if (power <= minPower) break;

      Token opToken = current;
      advance();

      Expression right = parseExpression(power);
      result =
          new BinaryExpression(
              result, opToken.getLexeme(), right, span(result.getPosition(), right.getPosition()));
    }
    return result;
  }

  private Expression parsePrimary() {
    Token token = current;

    switch (token.getType()) {
      case NUMBER_LITERAL:
        {
          advance();
          double value = Double.parseDouble(token.getLexeme());
          return new NumberLiteral(value, token.getPosition());
        }

      case STRING_LITERAL:
        {
          advance();
          return new StringLiteral(token.getLexeme(), token.getPosition());
        }

      case IDENTIFIER:
        {
          advance();
          if (current.getType() == TokenType.LPAREN) {
            return parseCallExpression(token);
          }
          return new Identifier(token.getLexeme(), token.getPosition());
        }

      case LPAREN:
        {
          advance();
          Expression inner = parseExpression(0);
          consume(TokenType.RPAREN);
          return inner;
        }

      default:
        throw new ParseException(
            "Unexpected token '" + token.getLexeme() + "'", token.getPosition());
    }
  }

  private CallExpression parseCallExpression(Token nameToken) {
    consume(TokenType.LPAREN);
    List<Expression> args = new ArrayList<>();

    if (current.getType() != TokenType.RPAREN) {
      args.add(parseExpression(0));
    }

    Token close = current;
    consume(TokenType.RPAREN);
    return new CallExpression(
        nameToken.getLexeme(), args, span(nameToken.getPosition(), close.getPosition()));
  }

  // ---------------------------------------------------------------- Helpers

  private void consume(TokenType expected) {
    if (current.getType() != expected) {
      throw new ParseException(
          "Expected " + expected + " but found '" + current.getLexeme() + "'",
          current.getPosition());
    }
    advance();
  }

  private void advance() {
    if (tokens.hasNext()) {
      current = tokens.next();
    }
  }

  private Position span(Position start, Position end) {
    return new Position(
        start.getStartLine(), start.getStartColumn(), end.getEndLine(), end.getEndColumn());
  }
}
