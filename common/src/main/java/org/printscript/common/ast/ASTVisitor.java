package org.printscript.common.ast;

/**
 * Visitor del AST de PrintScript.
 *
 * <p>Cada módulo que necesite recorrer el árbol implementa esta interfaz: el interpreter, el
 * formatter, el analyzer, etc.
 *
 * @param <T> tipo de retorno de cada visita
 */
public interface ASTVisitor<T> {

  T visitProgram(Program node);

  T visitVarDeclaration(VarDeclarationStatement node);

  T visitAssignment(AssignmentStatement node);

  T visitExpressionStatement(ExpressionStatement node);

  T visitBinaryExpression(BinaryExpression node);

  T visitCallExpression(CallExpression node);

  T visitNumberLiteral(NumberLiteral node);

  T visitStringLiteral(StringLiteral node);

  T visitBooleanLiteral(BooleanLiteral node);

  T visitIdentifier(Identifier node);
}
