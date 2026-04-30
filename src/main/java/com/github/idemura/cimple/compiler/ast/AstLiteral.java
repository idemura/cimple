package com.github.idemura.cimple.compiler.ast;

import java.util.Objects;

public final class AstLiteral extends AstExpression {
  // String literal stores values as String.
  // Number(integer/float) literal - as (long/double), but actual type is defined by @type.
  private Object value;
  private TypeRef type;

  public static final AstLiteral TRUE = ofConst(AstTypeBuiltin.BOOL, true);
  public static final AstLiteral FALSE = ofConst(AstTypeBuiltin.BOOL, false);
  public static final AstLiteral NULL = ofConst(AstTypeBuiltin.NULL, null);

  public static AstLiteral ofInt(long value) {
    var literal = new AstLiteral();
    literal.setValue(value);
    literal.setType(TypeRef.of(AstTypeBuiltin.INT64));
    return literal;
  }

  public static AstLiteral ofBool(boolean value) {
    return value ? TRUE : FALSE;
  }

  public static AstLiteral ofNull() {
    return NULL;
  }

  public AstLiteral() {}

  @Override
  public Object accept(AstVisitor visitor) {
    return visitor.visit(this);
  }

  @Override
  public int hashCode() {
    return Objects.hash(getClass(), value);
  }

  @Override
  public boolean equals(Object object) {
    return this == object
        || (object instanceof AstLiteral other
            && Objects.equals(value, other.value)
            && Objects.equals(type, other.type));
  }

  public Object getValue() {
    return value;
  }

  public void setValue(Object value) {
    this.value = value;
  }

  public TypeRef getType() {
    return type;
  }

  public void setType(TypeRef type) {
    this.type = type;
  }

  public static AstLiteral ofConst(AstType type, Object value) {
    var literal = new AstLiteral();
    literal.setValue(value);
    literal.setType(TypeRef.of(type));
    return literal;
  }
}
