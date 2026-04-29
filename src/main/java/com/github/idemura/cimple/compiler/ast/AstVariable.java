package com.github.idemura.cimple.compiler.ast;

import com.github.idemura.cimple.compiler.TypeRef;

public final class AstVariable extends AstStatement {
  public static final long MUTABLE = 0x1L; // const/var
  public static final long PARAM = 0x2L; // Whether it is a function parameter.
  public static final long FIELD = 0x2L; // Whether it is a field.

  private String name;
  private TypeRef typeRef;
  private AstExpression expression;
  private long flags;

  public AstVariable() {}

  @Override
  public Object accept(AstVisitor visitor) {
    return visitor.visit(this);
  }

  public boolean getBit(long mask) {
    return (flags & mask) != 0;
  }

  public void setBit(long mask) {
    flags |= mask;
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
