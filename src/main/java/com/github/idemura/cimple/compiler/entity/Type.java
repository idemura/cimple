package com.github.idemura.cimple.compiler.entity;

import com.github.idemura.cimple.compiler.Identifier;

public abstract class Type {
  private final Identifier name;

  public Type(Identifier name) {
    this.name = name;
  }

  public Identifier name() {
    return name;
  }
}
