package com.github.idemura.cimple.compiler.ast;

public final class AstNumberLiteral extends AstLiteral {
  // For testing
  public static AstNumberLiteral of(long value) {
    return new AstNumberLiteral(Long.toString(value));
  }

  public AstNumberLiteral(Object value) {
    super(value);
    // Don't know the type yet.
  }
}
