package org.printscript.ast;

/**
 * Visitor del AST de PrintScript.
 *
 * <p>Lo implementan las fases que tienen que hacer algo con cada tipo de nodo: el análisis
 * semántico, el intérprete y el formatter, que tiene que saber escribirlo (ver ADR-029). Las
 * herramientas que solo buscan ciertos nodos (el linter) recorren el árbol con {@link
 * Node#children()}, así un nodo nuevo no las obliga a cambiar (ver ADR-027).
 *
 * @param <T> tipo de retorno de cada visita
 */
public interface ASTVisitor<T> {

  T visitVarDeclaration(VarDeclarationStatement node);

  T visitAssignment(AssignmentStatement node);

  T visitExpressionStatement(ExpressionStatement node);

  T visitIf(IfStatement node);

  T visitBlock(Block node);

  T visitBinaryExpression(BinaryExpression node);

  T visitCallExpression(CallExpression node);

  T visitGroup(GroupExpression node);

  T visitNumberLiteral(NumberLiteral node);

  T visitStringLiteral(StringLiteral node);

  T visitBooleanLiteral(BooleanLiteral node);

  T visitIdentifier(Identifier node);
}
