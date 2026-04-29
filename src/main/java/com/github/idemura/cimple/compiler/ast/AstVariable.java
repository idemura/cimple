package com.github.idemura.cimple.compiler.ast;

import com.github.idemura.cimple.compiler.TypeRef;

public final class AstVariable extends AstStatement {
  public static final long BIT_MUTABLE = 0x1L;
  public static final long BIT_ARGUMENT = 0x2L;
  public static final long BIT_FIELD = 0x2L;

  private boolean mutable;
  private String name;
  private TypeRef typeRef;
  private AstExpression expression;

  public AstVariable() {}

  @Override
  public Object accept(AstVisitor visitor) {
    return visitor.visit(this);
  }

  public boolean getMutable() {
    return mutable;
  }

  public void setMutable(boolean mutable) {
    this.mutable = mutable;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setTypeRef(TypeRef typeRef) {
    this.typeRef = typeRef;
  }

  public TypeRef getTypeRef() {
    return typeRef;
  }

  public void setExpression(AstExpression expression) {
    this.expression = expression;
  }

  public AstExpression getExpression() {
    return expression;
  }
}
