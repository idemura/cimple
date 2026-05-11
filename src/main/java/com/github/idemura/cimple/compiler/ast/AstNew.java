package com.github.idemura.cimple.compiler.ast;

public final class AstNew extends AstExpression {
  private AstType type;
  private AstExpression size;

  @Override
  public Object accept(AstVisitor visitor) {
    return visitor.visit(this);
  }

  @Override
  public AstType type() {
    return type;
  }

  public void type(AstType type) {
    this.type = type;
  }

  public AstExpression size() {
    return size;
  }

  public void size(AstExpression size) {
    this.size = size;
  }
}
