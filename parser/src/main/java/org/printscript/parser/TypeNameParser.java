package org.printscript.parser;

import java.util.Set;
import org.printscript.common.token.Token;
import org.printscript.common.token.TokenType;

/**
 * Resuelve el nombre de tipo en una declaración a partir del token actual, validándolo contra un
 * conjunto de {@code TokenType} admitidos como tipo. Reemplaza la cadena de {@code if} original:
 * admitir un tipo nuevo = agregarlo al conjunto, sin tocar la lógica.
 *
 * <p>El nombre devuelto es el lexema real del token (igual que la implementación original), no un
 * valor fijo.
 */
public final class TypeNameParser {

  private final Set<TokenType> typeTokens;

  public TypeNameParser(Set<TokenType> typeTokens) {
    this.typeTokens = Set.copyOf(typeTokens);
  }

  /** Consume el token de tipo actual y devuelve su lexema. Fail-fast si no es un tipo válido. */
  public String parse(TokenStream tokens) {
    Token token = tokens.current();
    if (!typeTokens.contains(token.getType())) {
      throw new ParseException(
          "Expected type name, found '" + token.getLexeme() + "'", token.getPosition());
    }
    tokens.advance();
    return token.getLexeme();
  }
}
