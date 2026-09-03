package io.lang.cimple.compiler;

public abstract sealed class AstEntity extends AstNode permits AstFunction, AstVariable {
  public AstEntity(Location location) {
    super(location);
  }

  public abstract Identifier name();

  public abstract AstType type();
}
