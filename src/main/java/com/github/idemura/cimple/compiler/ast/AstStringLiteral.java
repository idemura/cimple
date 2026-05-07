package com.github.idemura.cimple.compiler.ast;

public final class AstStringLiteral extends AstLiteral {
  public AstStringLiteral(String value) {
    super(value);
  }

  @Override
  public Object accept(AstVisitor visitor) {
    return visitor.visit(this);
  }
}
