package com.github.idemura.cimple.compiler.ast;

public abstract sealed class AstExpression extends AstNode
    permits AstApplyFunction, AstLiteral, AstNameRef {
  protected AstExpression() {}

  public abstract AstType getType();
}
