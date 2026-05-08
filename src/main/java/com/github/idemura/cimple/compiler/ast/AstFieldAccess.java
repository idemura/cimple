package com.github.idemura.cimple.compiler.ast;

public final class AstFieldAccess extends AstExpression {
  private AstExpression object;
  private String fieldName;

  @Override
  public Object accept(AstVisitor visitor) {
    return visitor.visit(this);
  }

  @Override
  public AstTypeRef typeRef() {
    throw new UnsupportedOperationException();
  }

  public AstExpression object() {
    return object;
  }

  public void object(AstExpression object) {
    this.object = object;
  }

  public String fieldName() {
    return fieldName;
  }

  public void fieldName(String fieldName) {
    this.fieldName = fieldName;
  }
}
