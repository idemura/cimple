package com.github.idemura.cimple.compiler.ast;

import com.github.idemura.cimple.compiler.TypeRef;

public abstract class AstExpression extends AstNode {
  protected AstExpression() {}

  public abstract TypeRef getTypeRef();
}
