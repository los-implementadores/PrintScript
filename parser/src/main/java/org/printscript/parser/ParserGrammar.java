package org.printscript.parser;

import java.util.Map;
import java.util.Set;
import org.printscript.common.token.TokenType;
import org.printscript.parser.parselet.InfixParselet;
import org.printscript.parser.parselet.PrefixParselet;
import org.printscript.parser.parselet.StatementParselet;
import org.printscript.parser.parselet.infix.BinaryOperatorParselet;
import org.printscript.parser.parselet.prefix.GroupParselet;
import org.printscript.parser.parselet.prefix.IdentifierParselet;
import org.printscript.parser.parselet.prefix.NumberLiteralParselet;
import org.printscript.parser.parselet.prefix.StringLiteralParselet;
import org.printscript.parser.parselet.statement.AssignmentOrExpressionParselet;
import org.printscript.parser.parselet.statement.ExpressionStatementParselet;
import org.printscript.parser.parselet.statement.VarDeclarationParselet;

/**
 * Conjunto de parselets que definen la gramática. Este es el <em>único</em> punto donde se registra
 * qué construcciones entiende el parser: agregar una sentencia, expresión u operador se hace acá,
 * sin tocar {@link ParserImpl} (Open/Closed).
 *
 * <p>Las precedencias (binding power) de los operadores viven en {@link #ADDITIVE} y {@link
 * #MULTIPLICATIVE}, reemplazando el antiguo {@code switch} de precedencias por datos.
 */
public final class ParserGrammar {

  private static final int ADDITIVE = 10; // + -
  private static final int MULTIPLICATIVE = 20; // * /

  private final Map<TokenType, StatementParselet> statementParselets;
  private final Map<TokenType, PrefixParselet> prefixParselets;
  private final Map<TokenType, InfixParselet> infixParselets;
  private final StatementParselet defaultStatementParselet;

  public ParserGrammar(
      Map<TokenType, StatementParselet> statementParselets,
      Map<TokenType, PrefixParselet> prefixParselets,
      Map<TokenType, InfixParselet> infixParselets,
      StatementParselet defaultStatementParselet) {
    this.statementParselets = Map.copyOf(statementParselets);
    this.prefixParselets = Map.copyOf(prefixParselets);
    this.infixParselets = Map.copyOf(infixParselets);
    this.defaultStatementParselet = defaultStatementParselet;
  }

  /** Gramática por defecto de PrintScript 1.0. */
  public static ParserGrammar printScript() {
    TypeNameParser typeNameParser =
        new TypeNameParser(Set.of(TokenType.TYPE_NUMBER, TokenType.TYPE_STRING));

    Map<TokenType, StatementParselet> statements =
        Map.of(
            TokenType.LET, new VarDeclarationParselet(typeNameParser),
            TokenType.IDENTIFIER, new AssignmentOrExpressionParselet());

    Map<TokenType, PrefixParselet> prefixes =
        Map.of(
            TokenType.NUMBER_LITERAL, new NumberLiteralParselet(),
            TokenType.STRING_LITERAL, new StringLiteralParselet(),
            TokenType.IDENTIFIER, new IdentifierParselet(),
            TokenType.LPAREN, new GroupParselet());

    Map<TokenType, InfixParselet> infixes =
        Map.of(
            TokenType.PLUS, new BinaryOperatorParselet(ADDITIVE),
            TokenType.MINUS, new BinaryOperatorParselet(ADDITIVE),
            TokenType.STAR, new BinaryOperatorParselet(MULTIPLICATIVE),
            TokenType.SLASH, new BinaryOperatorParselet(MULTIPLICATIVE));

    return new ParserGrammar(statements, prefixes, infixes, new ExpressionStatementParselet());
  }

  Map<TokenType, StatementParselet> statementParselets() {
    return statementParselets;
  }

  Map<TokenType, PrefixParselet> prefixParselets() {
    return prefixParselets;
  }

  Map<TokenType, InfixParselet> infixParselets() {
    return infixParselets;
  }

  StatementParselet defaultStatementParselet() {
    return defaultStatementParselet;
  }
}
