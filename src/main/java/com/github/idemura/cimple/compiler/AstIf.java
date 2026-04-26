package com.github.idemura.cimple.compiler;

import java.util.ArrayList;
import java.util.List;

public class AstIf extends AstStatement {
  private List<AstExpression> conditions = new ArrayList<>();
  private List<AstBlock> thenBlocks = new ArrayList<>();
  private AstBlock elseBlock;

  AstIf() {}

  @Override
  public void accept(Visitor visitor) {
    visitor.visit(this);
  }

  void addIf(AstExpression condition, AstBlock block) {
    conditions.add(condition);
    thenBlocks.add(block);
  }

  List<AstExpression> getConditions() {
    return conditions;
  }

  List<AstBlock> getThenBlocks() {
    return thenBlocks;
  }

  void setElseBlock(AstBlock elseBlock) {
    this.elseBlock = elseBlock;
  }

  AstBlock getElseBlock() {
    return elseBlock;
  }
}
