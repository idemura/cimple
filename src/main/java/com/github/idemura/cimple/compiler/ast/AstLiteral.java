package com.github.idemura.cimple.compiler.ast;

import com.github.idemura.cimple.compiler.BuiltinType;
import com.github.idemura.cimple.compiler.Type;
import java.util.Objects;

public final class AstLiteral extends AstExpression {
  // String literal stores values as String.
  // Number(integer/float) literal - as (long/double), but actual type is defined by @type.
  private Object value;
  private Type type;

  public static final AstLiteral TRUE = ofConst(BuiltinType.BOOL, true);
  public static final AstLiteral FALSE = ofConst(BuiltinType.BOOL, false);
  public static final AstLiteral NULL = ofConst(BuiltinType.NULL, null);

  public static AstLiteral ofInt(long value) {
    var l = new AstLiteral();
    l.setValue(value);
    l.setType(BuiltinType.INT64);
    return l;
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

  public Type getType() {
    return type;
  }

  public void setType(Type type) {
    this.type = type;
  }

  public static AstLiteral ofConst(Type type, Object value) {
    var l = new AstLiteral();
    l.setValue(value);
    l.setType(type);
    return l;
  }
}
