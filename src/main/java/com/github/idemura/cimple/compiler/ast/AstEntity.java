package com.github.idemura.cimple.compiler.ast;

import com.github.idemura.cimple.compiler.Identifier;

public abstract sealed class AstEntity extends AstNode permits AstFunction, AstVariable {
  public AstEntity() {}

  public abstract Identifier name();

  public abstract void name(Identifier name);

  public abstract AstType type();
}
