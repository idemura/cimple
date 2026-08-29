package io.lang.cimple.compiler.ast;

public final class AstFor extends AstStatement {
  private AstLocal init;
  private AstExpressionHolder condition;
  private AstExpressionHolder increment;
  private AstBlock block;

  public AstFor() {}

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

  public void init(AstLocal init) {
    this.init = init;
  }

  public AstExpressionHolder condition() {
    return condition;
  }

  public void condition(AstExpressionHolder condition) {
    this.condition = condition;
  }

  public AstExpressionHolder increment() {
    return increment;
  }

  public void increment(AstExpressionHolder increment) {
    this.increment = increment;
  }

  public AstBlock block() {
    return block;
  }

  public void block(AstBlock block) {
    this.block = block;
  }
}
