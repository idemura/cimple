package com.github.idemura.cimple.compiler.ast;

import com.github.idemura.cimple.compiler.TypeRef;

public final class AstVariable extends AstStatement {
  public static final long BIT_MUTABLE = 0x1L;
  public static final long BIT_ARGUMENT = 0x2L;
  public static final long BIT_FIELD = 0x2L;

  private String name;
  private TypeRef typeRef;
  private AstExpression expression;
  private long flags;

  public AstVariable() {}

  @Override
  public Object accept(AstVisitor visitor) {
    return visitor.visit(this);
  }

  public boolean isMutable() {
    return checkFlag(BIT_MUTABLE);
  }

  public void setMutable(boolean mutable) {
    setFlag(BIT_MUTABLE, mutable);
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

  private boolean checkFlag(long mask) {
    return (flags & mask) != 0;
  }

  private void setFlag(long mask, boolean toSet) {
    flags = (flags & ~mask) | (toSet ? mask : 0);
  }
}
