package io.lang.cimple.compiler.ast;

import io.lang.cimple.compiler.Identifier;

public abstract sealed class AstEntity extends AstNode permits AstFunction, AstVariable {
  public AstEntity() {}

  public abstract Identifier name();

  public abstract void name(Identifier name);

  public abstract AstType type();
}
