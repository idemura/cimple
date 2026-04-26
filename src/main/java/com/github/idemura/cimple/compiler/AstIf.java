package com.github.idemura.cimple.compiler;

import java.util.ArrayList;
import java.util.List;

public class AstIf extends AstAbstractStatement {
  private List<AstAbstractExpression> conditions = new ArrayList<>();
  private List<AstBlock> thenBlocks = new ArrayList<>();
  private AstBlock elseBlock;

  AstIf() {}

  @Override
  public void accept(AstVisitor visitor) {
    visitor.visit(this);
  }

  void addIf(AstAbstractExpression condition, AstBlock block) {
    conditions.add(condition);
    thenBlocks.add(block);
  }

  List<AstAbstractExpression> getConditions() {
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
