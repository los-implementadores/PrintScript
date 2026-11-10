package org.printscript.parser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.printscript.common.token.TokenType;
import org.printscript.parser.parselet.InfixParselet;
import org.printscript.parser.parselet.PrefixParselet;
import org.printscript.parser.parselet.StatementParselet;
import org.printscript.parser.parselet.statement.ExpressionStatementParselet;

/**
 * Lo que entiende un parser: qué parselet maneja cada token y qué tokens son nombres de tipo.
 * Agregar una sentencia, expresión u operador es registrar un parselet, sin tocar {@link
 * ParserImpl} (Open/Closed).
 *
 * <p>El parser no sabe de versiones del lenguaje. Una gramática se arma con las {@link
 * GrammarFeature} que se quieran soportar, y quien orquesta decide cuáles corresponden a cada
 * versión.
 */
public final class ParserGrammar {

  private final Map<TokenType, StatementParselet> statementParselets;
  private final Map<TokenType, PrefixParselet> prefixParselets;
  private final Map<TokenType, InfixParselet> infixParselets;
  private final Set<TokenType> typeNames;
  private final StatementParselet defaultStatementParselet;

  private ParserGrammar(Builder builder) {
    this.statementParselets = Map.copyOf(builder.statements);
    this.prefixParselets = Map.copyOf(builder.prefixes);
    this.infixParselets = Map.copyOf(builder.infixes);
    this.typeNames = Set.copyOf(builder.typeNames);
    this.defaultStatementParselet = builder.defaultStatement;
  }

  /** Gramática con las construcciones de las features dadas. */
  public static ParserGrammar of(GrammarFeature... features) {
    Builder builder = builder();
    for (GrammarFeature feature : features) {
      feature.addTo(builder);
    }
    return builder.build();
  }

  public static Builder builder() {
    return new Builder();
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

  Set<TokenType> typeNames() {
    return typeNames;
  }

  StatementParselet defaultStatementParselet() {
    return defaultStatementParselet;
  }

  /** Arma una gramática de a una construcción. */
  public static final class Builder {

    private final Map<TokenType, StatementParselet> statements = new HashMap<>();
    private final Map<TokenType, PrefixParselet> prefixes = new HashMap<>();
    private final Map<TokenType, InfixParselet> infixes = new HashMap<>();
    private final Set<TokenType> typeNames = new HashSet<>();
    private StatementParselet defaultStatement = new ExpressionStatementParselet();

    private Builder() {}

    /** Sentencia que empieza con {@code token}. */
    public Builder statement(TokenType token, StatementParselet parselet) {
      statements.put(token, parselet);
      return this;
    }

    /** Expresión que empieza con {@code token}. */
    public Builder prefix(TokenType token, PrefixParselet parselet) {
      prefixes.put(token, parselet);
      return this;
    }

    /** Operador infijo {@code token}, con su precedencia en el parselet. */
    public Builder infix(TokenType token, InfixParselet parselet) {
      infixes.put(token, parselet);
      return this;
    }

    /** Token que se acepta como nombre de tipo en una declaración. */
    public Builder typeName(TokenType token) {
      typeNames.add(token);
      return this;
    }

    /** Parselet para las sentencias que no empiezan con ningún token registrado. */
    public Builder defaultStatement(StatementParselet parselet) {
      defaultStatement = parselet;
      return this;
    }

    public ParserGrammar build() {
      return new ParserGrammar(this);
    }
  }
}
