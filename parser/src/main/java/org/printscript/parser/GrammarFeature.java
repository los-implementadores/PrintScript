package org.printscript.parser;

import org.printscript.common.token.TokenType;
import org.printscript.parser.parselet.infix.BinaryOperatorParselet;
import org.printscript.parser.parselet.prefix.BooleanLiteralParselet;
import org.printscript.parser.parselet.prefix.GroupParselet;
import org.printscript.parser.parselet.prefix.IdentifierParselet;
import org.printscript.parser.parselet.prefix.NumberLiteralParselet;
import org.printscript.parser.parselet.prefix.StringLiteralParselet;
import org.printscript.parser.parselet.statement.AssignmentOrExpressionParselet;
import org.printscript.parser.parselet.statement.IfStatementParselet;
import org.printscript.parser.parselet.statement.VarDeclarationParselet;

/**
 * Las construcciones que sabe parsear el parser, agrupadas para armar una {@link ParserGrammar}.
 *
 * <p>Es el único lugar donde se registra qué parselet maneja cada construcción. Las features no
 * saben de versiones del lenguaje: cada una es algo que el parser puede entender, y quien orquesta
 * elige cuáles se usan.
 */
public enum GrammarFeature {

  /**
   * Declaraciones con {@code let}, asignaciones, llamadas, los tipos {@code number} y {@code
   * string}, sus literales y las operaciones {@code + - * /} con paréntesis.
   */
  CORE {
    @Override
    void addTo(ParserGrammar.Builder grammar) {
      grammar
          .typeName(TokenType.TYPE_NUMBER)
          .typeName(TokenType.TYPE_STRING)
          .statement(TokenType.LET, new VarDeclarationParselet(TokenType.LET, false))
          .statement(TokenType.IDENTIFIER, new AssignmentOrExpressionParselet())
          .prefix(TokenType.NUMBER_LITERAL, new NumberLiteralParselet())
          .prefix(TokenType.STRING_LITERAL, new StringLiteralParselet())
          .prefix(TokenType.IDENTIFIER, new IdentifierParselet())
          .prefix(TokenType.LPAREN, new GroupParselet())
          .infix(TokenType.PLUS, new BinaryOperatorParselet(ADDITIVE))
          .infix(TokenType.MINUS, new BinaryOperatorParselet(ADDITIVE))
          .infix(TokenType.STAR, new BinaryOperatorParselet(MULTIPLICATIVE))
          .infix(TokenType.SLASH, new BinaryOperatorParselet(MULTIPLICATIVE));
    }
  },

  /** El tipo {@code boolean} y los literales {@code true} y {@code false}. */
  BOOLEANS {
    @Override
    void addTo(ParserGrammar.Builder grammar) {
      grammar
          .typeName(TokenType.TYPE_BOOLEAN)
          .prefix(TokenType.BOOLEAN_LITERAL, new BooleanLiteralParselet());
    }
  },

  /** Declaraciones inmutables con {@code const}. */
  CONSTANTS {
    @Override
    void addTo(ParserGrammar.Builder grammar) {
      grammar.statement(TokenType.CONST, new VarDeclarationParselet(TokenType.CONST, true));
    }
  },

  /** Condicionales {@code if (cond) { ... } else { ... }}. */
  CONDITIONALS {
    @Override
    void addTo(ParserGrammar.Builder grammar) {
      grammar.statement(TokenType.IF, new IfStatementParselet());
    }
  };

  /** Precedencia (binding power) de {@code + -}. */
  private static final int ADDITIVE = 10;

  /** Precedencia (binding power) de {@code * /}. */
  private static final int MULTIPLICATIVE = 20;

  /** Registra los parselets y tipos de esta feature en la gramática. */
  abstract void addTo(ParserGrammar.Builder grammar);
}
