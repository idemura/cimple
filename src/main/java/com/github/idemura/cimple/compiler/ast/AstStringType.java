package com.github.idemura.cimple.compiler.ast;

import com.github.idemura.cimple.compiler.Identifier;

public final class AstStringType extends AstType {
  public static final AstStringType STRING = new AstStringType();

  private final Identifier name = Identifier.ofType("string").builtin();

  private AstStringType() {}

  @Override
  public void accept(AstVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public void acceptChildren(AstVisitor visitor) {}

  @Override
  public Identifier name() {
    return name;
  }

  @Override
  public void name(Identifier name) {
    throw new UnsupportedOperationException();
  }
}
