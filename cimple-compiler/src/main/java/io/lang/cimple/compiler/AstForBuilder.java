package io.lang.cimple.compiler;

public final class AstForBuilder {
  private Location location;
  private AstLocal init;
  private AstExpression condition;
  private AstExpression increment;
  private AstBlock block;

  public AstForBuilder() {}

  public void location(Location location) {
    this.location = location;
  }

  public void init(AstLocal init) {
    this.init = init;
  }

  public void condition(AstExpression condition) {
    this.condition = condition;
  }

  public void increment(AstExpression increment) {
    this.increment = increment;
  }

  public void block(AstBlock block) {
    this.block = block;
  }

  public AstFor build() {
    return new AstFor(location, init, condition, increment, block);
  }
}
