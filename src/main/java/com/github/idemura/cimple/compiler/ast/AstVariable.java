package com.github.idemura.cimple.compiler.ast;

import com.github.idemura.cimple.compiler.QualifiedName;
import java.util.Objects;

public final class AstVariable extends AstEntity {
  public static final long MUTABLE = 0x1L;
  public static final long PARAMETER = 0x2L;
  public static final long LOCAL = 0x4L;
  public static final long FIELD = 0x8L;
  public static final long GLOBAL = 0x10L;

  private QualifiedName name;
  private AstTypeRef typeRef;
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

  public boolean isAnyOf(long mask) {
    return (flags & mask) != 0;
  }

  public void setBit(long mask) {
    flags |= mask;
  }

  public QualifiedName name() {
    return name;
  }

  public void name(QualifiedName name) {
    this.name = name;
  }

  public AstTypeRef typeRef() {
    return typeRef;
  }

  public void typeRef(AstTypeRef typeRef) {
    this.typeRef = typeRef;
  }

  public AstExpression expression() {
    return expression;
  }

  public void expression(AstExpression expression) {
    this.expression = expression;
  }
}
