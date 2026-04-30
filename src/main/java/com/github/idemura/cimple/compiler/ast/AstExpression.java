package com.github.idemura.cimple.compiler.ast;

public abstract sealed class AstExpression extends AstNode
    permits AstCall, AstArrayAccess, AstBind, AstFieldAccess, AstLiteral, AstName, AstCast {
  protected AstExpression() {}

  public abstract AstType getType();
}
