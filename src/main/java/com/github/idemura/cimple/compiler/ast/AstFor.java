package com.github.idemura.cimple.compiler.ast;

import java.util.Objects;

public final class AstFor extends AstStatement {
  private AstVariable init;
  private AstExpressionRoot condition;
  private AstExpressionRoot increment;
  private AstBlock block;

  public AstFor() {}

  @Override
  public Object accept(AstVisitor visitor) {
    return visitor.visit(this);
  }

  public AstVariable init() {
    return init;
  }

  public void init(AstVariable init) {
    this.init = init;
  }

  public AstExpressionRoot condition() {
    return condition;
  }

  public void condition(AstExpressionRoot condition) {
    this.condition = condition;
  }

  public AstExpressionRoot increment() {
    return increment;
  }

  public void increment(AstExpressionRoot increment) {
    this.increment = increment;
  }

  public AstBlock block() {
    return block;
  }

  public void block(AstBlock block) {
    this.block = block;
  }
}
