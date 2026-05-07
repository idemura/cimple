package com.github.idemura.cimple.compiler.ast;

public final class AstNullLiteral extends AstLiteral {
  public AstNullLiteral() {
    super(null);
  }

  @Override
  public Object accept(AstVisitor visitor) {
    return visitor.visit(this);
  }
}
