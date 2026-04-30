package com.github.idemura.cimple.compiler.ast;

public abstract sealed class AstExpression extends AstNode
    permits AstApplyFunction,
    AstArrayAccess, AstBind, AstFieldAccess, AstLiteral, AstNameRef, AstTypeCast {
  protected AstExpression() {}

  public abstract AstType getType();
}
