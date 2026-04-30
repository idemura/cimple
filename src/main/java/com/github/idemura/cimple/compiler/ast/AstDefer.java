package com.github.idemura.cimple.compiler.ast;

public final class AstDefer extends AstStatement {
  private AstBlock block;

  public AstDefer() {}

  @Override
  public Object accept(AstVisitor visitor) {
    return visitor.visit(this);
  }

  public AstBlock getBlock() {
    return block;
  }

  public void setBlock(AstBlock block) {
    this.block = block;
  }
}
