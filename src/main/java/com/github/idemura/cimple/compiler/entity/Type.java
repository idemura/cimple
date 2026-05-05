package com.github.idemura.cimple.compiler.entity;

import com.github.idemura.cimple.compiler.QualifiedName;

public abstract class Type {
  private final QualifiedName name;

  public Type(QualifiedName name) {
    this.name = name;
  }

  public QualifiedName name() {
    return name;
  }
}
