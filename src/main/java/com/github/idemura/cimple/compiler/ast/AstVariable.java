package com.github.idemura.cimple.compiler.ast;

import com.github.idemura.cimple.compiler.Identifier;
import java.util.Objects;

public final class AstVariable extends AstEntity {
  public static final long MUTABLE = 0x1L;
  public static final long PARAMETER = 0x2L;
  public static final long LOCAL = 0x4L;
  public static final long FIELD = 0x8L;
  public static final long GLOBAL = 0x10L;

  private Identifier name;
  private AstType type;
  private AstExpressionHolder expression;
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
            && Objects.equals(type, other.type)
            && flags == other.flags);
  }

  @Override
  public Identifier name() {
    return name;
  }

  @Override
  public void name(Identifier name) {
    this.name = name;
  }

  @Override
  public AstType type() {
    return type;
  }

  public void type(AstType type) {
    this.type = type;
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

  public AstExpressionHolder expression() {
    return expression;
  }

  public void expression(AstExpressionHolder expression) {
    this.expression = expression;
  }
}
