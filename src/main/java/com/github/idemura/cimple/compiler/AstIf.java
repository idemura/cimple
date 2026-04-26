package com.github.idemura.cimple.compiler;

public class AstIf extends AstStatement {
  private AstExpression condition;
  private AstBlock thenBlock;
  private AstBlock elseBlock;

  AstIf() {}

  @Override
  public void accept(Visitor visitor) {
    visitor.visit(this);
  }

  void setCondition(AstExpression condition) {
    this.condition = condition;
  }

  AstExpression getCondition() {
    return condition;
  }

  void setThenBlock(AstBlock thenBlock) {
    this.thenBlock = thenBlock;
  }

  AstBlock getThenBlock() {
    return thenBlock;
  }

  void setElseBlock(AstBlock elseBlock) {
    this.elseBlock = elseBlock;
  }

  AstBlock getElseBlock() {
    return elseBlock;
  }
}
