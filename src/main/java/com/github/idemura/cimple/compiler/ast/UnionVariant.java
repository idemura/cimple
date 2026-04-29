package com.github.idemura.cimple.compiler.ast;

import java.util.Objects;

public final class UnionVariant {
  private QualifiedName name;
  private TypeRef valueType;

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public boolean equals(Object object) {
    return this == object
        || (object instanceof UnionVariant other
            && Objects.equals(name, other.name)
            && Objects.equals(valueType, other.valueType));
  }

  public QualifiedName getName() {
    return name;
  }

  public void setName(String name) {
    this.name = new QualifiedName(name);
  }

  public void setName(QualifiedName name) {
    this.name = name;
  }

  public TypeRef getValueType() {
    return valueType;
  }

  public void setValueType(TypeRef valueType) {
    this.valueType = valueType;
  }
}
