package io.lang.cimple.compiler;

public final class AstDefer extends AstStatement {
  private final AstBlock block;

  public AstDefer(Location location, AstBlock block) {
    super(location);
    this.block = block;
  }

  @Override
  public void accept(AstVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public void acceptChildren(AstVisitor visitor) {
    block.accept(visitor);
  }

  public AstBlock block() {
    return block;
  }
}
