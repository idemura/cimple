package com.github.idemura.cimple.compiler.ast;

import java.util.Objects;

public final class AstFor extends AstStatement {
  private AstVariable init;
  private AstExpressionHolder condition;
  private AstExpressionHolder increment;
  private AstBlock block;

  public AstFor() {}

  @Override
  public void accept(AstVisitor visitor) {
    visitor.visit(this);
  }

  public AstVariable init() {
    return init;
  }

  public void init(AstVariable init) {
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
