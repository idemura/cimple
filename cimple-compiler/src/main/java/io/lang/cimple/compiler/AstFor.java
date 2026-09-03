package io.lang.cimple.compiler;

public final class AstFor extends AstStatement {
  private final AstLocal init;
  private final AstExpressionHolder condition;
  private final AstExpressionHolder increment;
  private final AstBlock block;

  public AstFor(
      Location location,
      AstLocal init,
      AstExpression condition,
      AstExpression increment,
      AstBlock block) {
    super(location);
    this.init = init;
    this.condition = new AstExpressionHolder(condition);
    this.increment = new AstExpressionHolder(increment);
    this.block = block;
  }

  @Override
  public void accept(AstVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public void acceptChildren(AstVisitor visitor) {
    acceptSafe(init, visitor);
    acceptSafe(condition, visitor);
    acceptSafe(increment, visitor);
    block.accept(visitor);
  }

  public AstLocal init() {
    return init;
  }

  public AstExpression condition() {
    return condition.get();
  }

  public AstExpression increment() {
    return increment.get();
  }

  public AstBlock block() {
    return block;
  }
}
