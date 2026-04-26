package com.github.idemura.cimple.compiler.ast;

import com.github.idemura.cimple.compiler.Type;

public class AstString extends AstLiteral {
  public AstString(Object value) {
    super(value);
  }

  @Override
  public Type getType() {
    return null;
  }
}
