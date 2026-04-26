package com.github.idemura.cimple.compiler;

import java.util.Objects;

public class AstFor extends AstAbstractStatement {
  private AstVariable init;
  private AstAbstractExpression condition;
  private AstBlock block;

  AstFor() {}

  @Override
  public void accept(AstVisitor visitor) {
    visitor.visit(this);
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

  void setInit(AstVariable init) {
    this.init = init;
  }

  AstVariable getInit() {
    return init;
  }

  AstAbstractExpression getCondition() {
    return condition;
  }

  void setCondition(AstAbstractExpression condition) {
    this.condition = condition;
  }

  AstBlock getBlock() {
    return block;
  }

  void setBlock(AstBlock block) {
    this.block = block;
  }
}
