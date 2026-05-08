package com.github.idemura.cimple.compiler.ast;

import java.util.Objects;

public final class AstFor extends AstStatement {
  private AstVariable init;
  private AstExpression condition;
  private AstExpression increment;
  private AstBlock block;

  public AstFor() {}

  @Override
  public Object accept(AstVisitor visitor) {
    return visitor.visit(this);
  }

  @Override
  public int hashCode() {
    return Objects.hash(init, condition, increment, block);
  }

  @Override
  public boolean equals(Object object) {
    return this == object
        || (object instanceof AstFor other
            && Objects.equals(init, other.init)
            && Objects.equals(condition, other.condition)
            && Objects.equals(increment, other.increment)
            && Objects.equals(block, other.block));
  }

  public AstVariable init() {
    return init;
  }

  public void init(AstVariable init) {
    this.init = init;
  }

  public AstExpression condition() {
    return condition;
  }

  public void condition(AstExpression condition) {
    this.condition = condition;
  }

  public AstExpression increment() {
    return increment;
  }

  public void increment(AstExpression increment) {
    this.increment = increment;
  }

  public AstBlock block() {
    return block;
  }

  public void block(AstBlock block) {
    this.block = block;
  }
}
