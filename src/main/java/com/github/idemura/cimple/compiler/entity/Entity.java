package com.github.idemura.cimple.compiler.entity;

import com.github.idemura.cimple.compiler.QualifiedName;

public abstract sealed class Entity permits Function, Variable {
  private final QualifiedName name;
  private Type type;

  public Entity(QualifiedName name) {
    this.name = name;
  }

  @Override
  public int hashCode() {
    return name().hashCode();
  }

  @Override
  public boolean equals(Object object) {
    return this == object || (object instanceof Entity other && name.equals(other.name));
  }

  public QualifiedName name() {
    return name;
  }

  public Type getType() {
    return type;
  }

  public void setType(Type type) {
    this.type = type;
  }
}
