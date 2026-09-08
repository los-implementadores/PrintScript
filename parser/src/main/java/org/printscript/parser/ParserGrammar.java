package org.printscript.parser;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.printscript.common.LanguageVersion;
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

  /**
   * Gramática por defecto (PrintScript 1.0). Se mantiene por compatibilidad con los constructores
   * de {@code ParserImpl} que no especifican versión.
   */
  public static ParserGrammar printScript() {
    return forVersion(LanguageVersion.V1_0);
  }

  /**
   * Gramática de PrintScript para la versión indicada.
   *
   * <p>1.0 registra el conjunto base de construcciones. 1.1 parte de la base y agrega los parselets
   * propios de esa versión (boolean, const, if/else, readInput, readEnv). Cada feature nueva se
   * registra acá según su versión, sin tocar {@link ParserImpl} (Open/Closed).
   */
  public static ParserGrammar forVersion(LanguageVersion version) {
    Set<TokenType> typeTokens =
        version == LanguageVersion.V1_0
            ? Set.of(TokenType.TYPE_NUMBER, TokenType.TYPE_STRING)
            : Set.of(TokenType.TYPE_NUMBER, TokenType.TYPE_STRING, TokenType.TYPE_BOOLEAN);
    TypeNameParser typeNameParser = new TypeNameParser(typeTokens);

    Map<TokenType, StatementParselet> statements = new HashMap<>();
    statements.put(TokenType.LET, new VarDeclarationParselet(typeNameParser));
    statements.put(TokenType.IDENTIFIER, new AssignmentOrExpressionParselet());

    Map<TokenType, PrefixParselet> prefixes = new HashMap<>();
    prefixes.put(TokenType.NUMBER_LITERAL, new NumberLiteralParselet());
    prefixes.put(TokenType.STRING_LITERAL, new StringLiteralParselet());
    prefixes.put(TokenType.IDENTIFIER, new IdentifierParselet());
    prefixes.put(TokenType.LPAREN, new GroupParselet());

    Map<TokenType, InfixParselet> infixes = new HashMap<>();
    infixes.put(TokenType.PLUS, new BinaryOperatorParselet(ADDITIVE));
    infixes.put(TokenType.MINUS, new BinaryOperatorParselet(ADDITIVE));
    infixes.put(TokenType.STAR, new BinaryOperatorParselet(MULTIPLICATIVE));
    infixes.put(TokenType.SLASH, new BinaryOperatorParselet(MULTIPLICATIVE));

    if (version == LanguageVersion.V1_1) {
      registerV11(statements, prefixes, infixes, typeNameParser);
    }

    return new ParserGrammar(statements, prefixes, infixes, new ExpressionStatementParselet());
  }

  /**
   * Punto de extensión para las construcciones de PrintScript 1.1. Cada issue de 1.1 (boolean,
   * const, if/else, readInput, readEnv) registra acá sus parselets. Hoy es un no-op y se irá
   * completando a medida que esas features se implementen.
   */
  private static void registerV11(
      Map<TokenType, StatementParselet> statements,
      Map<TokenType, PrefixParselet> prefixes,
      Map<TokenType, InfixParselet> infixes,
      TypeNameParser typeNameParser) {
    // boolean (#31): prefixes.put(TokenType.BOOLEAN_LITERAL, new BooleanLiteralParselet());
    // const   (#32): statements.put(TokenType.CONST, new ConstDeclarationParselet(typeNameParser));
    // if/else (#33): statements.put(TokenType.IF, new IfStatementParselet());
    // readInput/readEnv (#34/#35): se resuelven como CallExpression sobre IDENTIFIER.
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
