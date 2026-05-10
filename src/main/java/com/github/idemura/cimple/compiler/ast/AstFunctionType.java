package com.github.idemura.cimple.compiler.ast;

import com.github.idemura.cimple.compiler.Identifier;

public final class AstFunctionType extends AstType {
  private Identifier name;
  private AstFunctionHeader header;

  @Override
  public Object accept(AstVisitor visitor) {
    return visitor.visit(this);
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public boolean equals(Object object) {
    return this == object
        || (object instanceof AstFunctionType other && header.equals(other.header()));
  }

  @Override
  public Identifier name() {
    return name;
  }

  public void name(Identifier name) {
    this.name = name;
  }

  public AstFunctionHeader header() {
    return header;
  }

  public void header(AstFunctionHeader header) {
    this.header = header;
  }
}
