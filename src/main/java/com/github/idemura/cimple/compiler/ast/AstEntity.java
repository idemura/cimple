package com.github.idemura.cimple.compiler.ast;

import com.github.idemura.cimple.compiler.QualifiedName;

public abstract sealed class AstEntity extends AstNode permits AstFunction, AstVariable {
  public AstEntity() {}

  public abstract QualifiedName name();

  public abstract void name(QualifiedName name);
}
