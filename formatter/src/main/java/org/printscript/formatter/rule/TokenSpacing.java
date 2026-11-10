package org.printscript.formatter.rule;

import java.util.Set;
import org.printscript.formatter.FormattingRule;
import org.printscript.formatter.Gap;
import org.printscript.token.TokenType;

/**
 * Fija el espacio antes y/o después de ciertos tokens, dentro de una línea: un espacio o ninguno.
 * Sirve para el {@code :} de las declaraciones, el {@code =} y los operadores. No toca los espacios
 * que tienen un salto de línea.
 */
public final class TokenSpacing implements FormattingRule {

  private final Set<TokenType> tokens;
  private final boolean before;
  private final boolean after;
  private final String space;

  private TokenSpacing(Set<TokenType> tokens, boolean before, boolean after, boolean withSpace) {
    this.tokens = Set.copyOf(tokens);
    this.before = before;
    this.after = after;
    this.space = withSpace ? " " : "";
  }

  /** Un espacio (o ninguno) antes de {@code token}. */
  public static TokenSpacing before(TokenType token, boolean withSpace) {
    return new TokenSpacing(Set.of(token), true, false, withSpace);
  }

  /** Un espacio (o ninguno) después de {@code token}. */
  public static TokenSpacing after(TokenType token, boolean withSpace) {
    return new TokenSpacing(Set.of(token), false, true, withSpace);
  }

  /** Un espacio (o ninguno) a cada lado de cualquiera de {@code tokens}. */
  public static TokenSpacing around(Set<TokenType> tokens, boolean withSpace) {
    return new TokenSpacing(tokens, true, true, withSpace);
  }

  @Override
  public String apply(Gap gap) {
    if (!gap.insideLine()) {
      return gap.whitespace();
    }
    boolean touchesToken =
        before && tokens.contains(gap.next().getType())
            || after && tokens.contains(gap.previous().getType());
    return touchesToken ? space : gap.whitespace();
  }
}
