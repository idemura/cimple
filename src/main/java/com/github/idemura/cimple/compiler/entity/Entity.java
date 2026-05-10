package com.github.idemura.cimple.compiler.entity;

import com.github.idemura.cimple.compiler.Identifier;

public abstract sealed class Entity permits Function, Variable {
  private final Identifier name;
  private Type type;

  public Entity(Identifier name) {
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

  public Identifier name() {
    return name;
  }

  public Type type() {
    return type;
  }

  public void type(Type type) {
    this.type = type;
  }
}
