package com.github.idemura.cimple.compiler.ast;

public abstract sealed class AstType extends AstNode
    permits AstTypeAlias, AstTypeBuiltin, AstTypeFunction, AstTypeRecord, AstTypeUnion {
  protected AstType() {}

  public abstract QualifiedName getName();
}
