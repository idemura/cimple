package com.github.idemura.cimple.compiler.ast;

import java.util.Objects;

public final class AstVariable extends AstStatement {
  public static final long MUTABLE = 0x1L; // const/var
  public static final long PARAM = 0x2L; // Whether it is a function parameter.
  public static final long FIELD = 0x4L; // Whether it is a field.

  private QualifiedName name;
  private TypeRef typeRef;
  private AstExpression expression;
  private long flags;

  public AstVariable() {}

  @Override
  public Object accept(AstVisitor visitor) {
    return visitor.visit(this);
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public boolean equals(Object object) {
    return this == object
        || (object instanceof AstVariable other
            && Objects.equals(name, other.name)
            && Objects.equals(typeRef, other.typeRef)
            && flags == other.flags);
  }

  public boolean getBit(long mask) {
    return (flags & mask) != 0;
  }

  public void setBit(long mask) {
    flags |= mask;
  }

  public void setName(String name) {
    this.name = new QualifiedName(name);
  }

  public void setName(QualifiedName name) {
    this.name = name;
  }

  public QualifiedName getName() {
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
