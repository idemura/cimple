package com.github.idemura.cimple.compiler.ast;

import java.util.ArrayList;
import java.util.List;

public class AstIf extends AstStatement {
  private List<AstExpression> conditions = new ArrayList<>();
  private List<AstBlock> thenBlocks = new ArrayList<>();
  private AstBlock elseBlock;

  public AstIf() {}

  @Override
  public void accept(AstVisitor visitor) {
    visitor.visit(this);
  }

  public void addIf(AstExpression condition, AstBlock block) {
    conditions.add(condition);
    thenBlocks.add(block);
  }

  public List<AstExpression> getConditions() {
    return conditions;
  }

  public List<AstBlock> getThenBlocks() {
    return thenBlocks;
  }

  public void setElseBlock(AstBlock elseBlock) {
    this.elseBlock = elseBlock;
  }

  public AstBlock getElseBlock() {
    return elseBlock;
  }
}
