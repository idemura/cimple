package com.github.idemura.cimple.compiler.ast;

public final class AstNullLiteral extends AstLiteral {
  public AstNullLiteral() {
    super(null);
    setType(AstTypeRef.of(AstBuiltinType.NULL));
  }
}
