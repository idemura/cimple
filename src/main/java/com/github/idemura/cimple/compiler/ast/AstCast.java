package com.github.idemura.cimple.compiler.ast;

public final class AstCast extends AstExpression {
  private AstExpression expression;
  private TypeRef typeRef;

  @Override
  public Object accept(AstVisitor visitor) {
    return visitor.visit(this);
  }

  @Override
  public TypeRef getType() {
    return typeRef;
  }

  public AstExpression getExpression() {
    return expression;
  }

  public void setExpression(AstExpression expression) {
    this.expression = expression;
  }

  public TypeRef getTypeRef() {
    return typeRef;
  }

  public void setTypeRef(TypeRef typeRef) {
    this.typeRef = typeRef;
  }
}
