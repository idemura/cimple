package com.github.idemura.cimple.compiler.ast;

public final class AstArrayAccess extends AstExpression {
  private AstExpression array;
  private AstExpression index;

  @Override
  public Object accept(AstVisitor visitor) {
    return visitor.visit(this);
  }

  @Override
  public AstType getType() {
    return null;
  }

  public AstExpression getArray() {
    return array;
  }

  public void setArray(AstExpression array) {
    this.array = array;
  }

  public AstExpression getIndex() {
    return index;
  }

  public void setIndex(AstExpression index) {
    this.index = index;
  }
}
