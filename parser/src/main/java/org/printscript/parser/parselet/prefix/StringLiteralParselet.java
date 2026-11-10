package org.printscript.parser.parselet.prefix;

import org.printscript.ast.Expression;
import org.printscript.ast.StringLiteral;
import org.printscript.parser.ParseContext;
import org.printscript.parser.parselet.PrefixParselet;
import org.printscript.token.Token;

/** Parsea un literal de cadena. El lexema trae las comillas; el valor es lo que está adentro. */
public final class StringLiteralParselet implements PrefixParselet {

  @Override
  public Expression parse(Token token, ParseContext ctx) {
    String lexeme = token.getLexeme();
    return new StringLiteral(lexeme.substring(1, lexeme.length() - 1), token.getPosition());
  }
}
