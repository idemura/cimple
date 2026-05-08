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

  public List<AstExpression> conditions() {
    return conditions;
  }

  public void conditions(List<AstExpression> conditions) {
    this.conditions = ImmutableList.copyOf(conditions);
  }

  public List<AstBlock> thenBlocks() {
    return thenBlocks;
  }

  public void thenBlocks(List<AstBlock> thenBlocks) {
    this.thenBlocks = ImmutableList.copyOf(thenBlocks);
  }

  public AstBlock elseBlock() {
    return elseBlock;
  }

  public void elseBlock(AstBlock elseBlock) {
    this.elseBlock = elseBlock;
  }
}
