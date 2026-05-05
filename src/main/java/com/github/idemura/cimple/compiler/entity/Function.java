package com.github.idemura.cimple.compiler.entity;

import com.github.idemura.cimple.compiler.QualifiedName;

public final class Function extends Entity {
  public Function(QualifiedName name) {
    super(name);
  }

  @Override
  public boolean equals(Object object) {
    return super.equals(object) && object instanceof Function;
  }
}
