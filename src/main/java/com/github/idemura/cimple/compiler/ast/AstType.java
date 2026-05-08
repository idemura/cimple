package com.github.idemura.cimple.compiler.ast;

import com.github.idemura.cimple.compiler.QualifiedName;

public abstract sealed class AstType extends AstNode
    permits AstBuiltinType, AstFunctionType, AstRecordType, AstUnionType {
  protected AstType() {}

  public abstract QualifiedName name();

  public abstract void name(QualifiedName name);
}
