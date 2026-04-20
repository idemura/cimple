package com.github.idemura.cimple.compiler;

public class AstIfElse extends AstStatement {
  private AstExpression condition;
  private AstBlock thenBlock;
  private AstBlock elseBlock;

  AstIfElse(Location location, AstExpression condition, AstBlock thenBlock, AstBlock elseBlock) {
    super(location);
    this.condition = condition;
    this.thenBlock = thenBlock;
    this.elseBlock = elseBlock;
  }

  @Override
  public void accept(Visitor visitor) {
    visitor.visit(this);
  }

  AstExpression getCondition() {
    return condition;
  }

  AstBlock getThenBlock() {
    return thenBlock;
  }

  AstBlock getElseBlock() {
    return elseBlock;
  }
}
