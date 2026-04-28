package com.github.idemura.cimple.compiler.ast;

import java.util.Objects;

public final class AstFor extends AstStatement {
  private AstVariable init;
  private AstExpression condition;
  private AstBlock block;

  public AstFor() {}

  @Override
  public Object accept(AstVisitor visitor) {
    return visitor.visit(this);
  }

  @Override
  public int hashCode() {
    return Objects.hash(init, condition, block);
  }

  @Override
  public boolean equals(Object object) {
    return this == object
        || (object instanceof AstFor other
            && Objects.equals(init, other.init)
            && Objects.equals(condition, other.condition)
            && Objects.equals(block, other.block));
  }

  public void setInit(AstVariable init) {
    this.init = init;
  }

  public AstVariable getInit() {
    return init;
  }

  public AstExpression getCondition() {
    return condition;
  }

  public void setCondition(AstExpression condition) {
    this.condition = condition;
  }

  public AstBlock getBlock() {
    return block;
  }

  public void setBlock(AstBlock block) {
    this.block = block;
  }
}
