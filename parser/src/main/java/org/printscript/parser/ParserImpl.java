package org.printscript.parser;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import org.printscript.common.ast.Expression;
import org.printscript.common.ast.Statement;
import org.printscript.common.token.Token;
import org.printscript.common.token.TokenType;
import org.printscript.parser.parselet.InfixParselet;
import org.printscript.parser.parselet.PrefixParselet;
import org.printscript.parser.parselet.StatementParselet;

/**
 * Implementación de {@link Parser}. Usa despacho por tablas (parselets) en vez de cadenas de {@code
 * if}/{@code switch}:
 *
 * <ul>
 *   <li>{@code Map<TokenType, StatementParselet>} para sentencias.
 *   <li>{@code Map<TokenType, PrefixParselet>} para expresiones prefijas (Pratt).
 *   <li>{@code Map<TokenType, InfixParselet>} para operadores infijos (Pratt), con su binding
 *       power.
 * </ul>
 *
 * <p>Agregar una construcción = registrar un parselet, sin modificar esta clase (Open/Closed). La
 * gestión de tokens vive en {@link TokenStream} (SRP). Los parselets solo dependen de {@link
 * ParseContext} (DIP).
 *
 * <p>Recibe un {@code Iterator<Token>} — no conoce al lexer. Implementa {@link Iterator}{@code
 * <Statement>}: produce una sentencia por llamada a {@link #next()}, sin materializar el árbol.
 */
public class ParserImpl implements Parser, ParseContext {

  private final TokenStream tokens;
  private final Map<TokenType, StatementParselet> statementParselets;
  private final Map<TokenType, PrefixParselet> prefixParselets;
  private final Map<TokenType, InfixParselet> infixParselets;
  private final StatementParselet defaultStatementParselet;

  /** Construye un parser con la gramática por defecto de PrintScript (1.0). */
  public ParserImpl(Iterator<Token> tokens) {
    this(tokens, ParserGrammar.printScript());
  }

  /** Construye un parser con la gramática correspondiente a la versión indicada. */
  public ParserImpl(Iterator<Token> tokens, org.printscript.common.LanguageVersion version) {
    this(tokens, ParserGrammar.forVersion(version));
  }

  /** Construye un parser con una gramática (conjunto de parselets) explícita. */
  public ParserImpl(Iterator<Token> tokens, ParserGrammar grammar) {
    this.tokens = new TokenStream(tokens);
    this.statementParselets = grammar.statementParselets();
    this.prefixParselets = grammar.prefixParselets();
    this.infixParselets = grammar.infixParselets();
    this.defaultStatementParselet = grammar.defaultStatementParselet();
  }

  // ---------------------------------------------------------------- Iterator API

  @Override
  public boolean hasNext() {
    return !tokens.atEnd();
  }

  @Override
  public Statement next() {
    if (!hasNext()) {
      throw new NoSuchElementException("No more statements");
    }
    return parseStatement();
  }

  // ---------------------------------------------------------------- Statement dispatch

  private Statement parseStatement() {
    StatementParselet parselet = statementParselets.get(tokens.currentType());
    if (parselet == null) {
      parselet = defaultStatementParselet;
    }
    return parselet.parse(this);
  }

  // ---------------------------------------------------------------- ParseContext

  @Override
  public TokenStream tokens() {
    return tokens;
  }

  @Override
  public Expression parseExpression(int minBindingPower) {
    Token token = tokens.advance();
    PrefixParselet prefix = prefixParselets.get(token.getType());
    if (prefix == null) {
      throw new ParseException("Unexpected token '" + token.getLexeme() + "'", token.getPosition());
    }
    Expression left = prefix.parse(token, this);
    return parseInfix(left, minBindingPower);
  }

  @Override
  public Expression parseInfix(Expression left, int minBindingPower) {
    Expression result = left;
    while (true) {
      InfixParselet infix = infixParselets.get(tokens.currentType());
      if (infix == null || infix.bindingPower() <= minBindingPower) {
        break;
      }
      Token opToken = tokens.advance();
      result = infix.parse(result, opToken, this);
    }
    return result;
  }
}
