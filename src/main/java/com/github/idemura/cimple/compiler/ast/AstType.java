package com.github.idemura.cimple.compiler.ast;

import com.github.idemura.cimple.compiler.Identifier;

public abstract sealed class AstType extends AstNode
    permits AstBuiltinType, AstFunctionType, AstRecordType, AstUnionType {
  protected AstType() {}

  public abstract Identifier name();

  public abstract void name(Identifier name);
}
