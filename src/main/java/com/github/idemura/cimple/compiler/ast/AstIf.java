package com.github.idemura.cimple.compiler.ast;

import com.google.common.collect.ImmutableList;
import java.util.List;

public final class AstIf extends AstStatement {
  private List<AstExpression> conditions;
  private List<AstBlock> thenBlocks;
  private AstBlock elseBlock;

  public AstIf() {}

  @Override
  public Object accept(AstVisitor visitor) {
    return visitor.visit(this);
  }

  public List<AstExpression> getConditions() {
    return conditions;
  }

  public void setConditions(List<AstExpression> conditions) {
    this.conditions = ImmutableList.copyOf(conditions);
  }

  public List<AstBlock> getThenBlocks() {
    return thenBlocks;
  }

  public void setThenBlocks(List<AstBlock> thenBlocks) {
    this.thenBlocks = ImmutableList.copyOf(thenBlocks);
  }

  public AstBlock getElseBlock() {
    return elseBlock;
  }

  public void setElseBlock(AstBlock elseBlock) {
    this.elseBlock = elseBlock;
  }
}
