package io.lang.cimple.compiler;

import java.util.Objects;

public final class AstVariable extends AstEntity {
  public static final long MUTABLE = 0x1L;
  public static final long PARAMETER = 0x2L;
  public static final long LOCAL = 0x4L;
  public static final long FIELD = 0x8L;
  public static final long GLOBAL = 0x10L;

  private final Identifier name;
  private final AstTypeHolder type;
  private final AstExpressionHolder expression;
  private long flags;

  public AstVariable(Identifier name, AstType type, AstExpression expression) {
    super(name.location());
    this.name = name;
    this.type = new AstTypeHolder(type);
    this.expression = new AstExpressionHolder(expression);
  }

  @Override
  public void accept(AstVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public void acceptChildren(AstVisitor visitor) {
    type.accept(visitor);
    expression.accept(visitor);
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
            && Objects.equals(type(), other.type())
            && flags == other.flags);
  }

  @Override
  public Identifier name() {
    return name;
  }

  @Override
  public AstType type() {
    return type.get();
  }

  public void type(AstType type) {
    this.type.set(type);
  }

  public boolean getBit(long mask) {
    return (flags & mask) != 0;
  }

  public boolean isAnyOf(long mask) {
    return (flags & mask) != 0;
  }

  public void flags(long mask) {
    flags |= mask;
  }

  public AstExpression expression() {
    return expression.get();
  }

  public void expression(AstExpression expression) {
    this.expression.set(expression);
  }
}
