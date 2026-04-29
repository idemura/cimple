package com.github.idemura.cimple.compiler.ast;

public abstract sealed class AstType extends AstNode
    permits AstTypeAlias, AstTypeBuiltin, AstTypeFunction, AstTypeStruct, AstTypeUnion {
  protected AstType() {}

  public abstract QualifiedName getName();
}
