package com.github.idemura.cimple.compiler.ast;

public final class AstBoolLiteral extends AstLiteral {
  public AstBoolLiteral(boolean value) {
    super(value);
    setType(AstTypeRef.of(AstBuiltinType.BOOL));
  }
}
