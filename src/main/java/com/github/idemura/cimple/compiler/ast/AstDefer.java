package com.github.idemura.cimple.compiler.ast;

public final class AstDefer extends AstStatement {
  private AstBlock block;

  public AstDefer() {}

  @Override
  public void accept(AstVisitor visitor) {
    visitor.visit(this);
  }

  public AstBlock block() {
    return block;
  }

  public void block(AstBlock block) {
    this.block = block;
  }
}
