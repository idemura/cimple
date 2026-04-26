package com.github.idemura.cimple.compiler.ast;

import com.github.idemura.cimple.compiler.Type;

public abstract class AstExpression extends AstNode {
  protected AstExpression() {}

  public abstract Type getType();
}
