package com.github.idemura.cimple.compiler.ast;

public abstract class AstExpression extends AstNode {
  protected AstExpression() {}

  public abstract AstType getType();
}
