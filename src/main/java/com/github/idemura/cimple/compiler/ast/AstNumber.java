package com.github.idemura.cimple.compiler.ast;

import com.github.idemura.cimple.compiler.Type;

public class AstNumber extends AstLiteral {
  public AstNumber(Object value) {
    super(value);
  }

  @Override
  public Type getType() {
    return null;
  }
}
