package io.lang.cimple.compiler;

import com.google.common.collect.ImmutableList;

public final class AstIfBuilder {
  private Location location;
  private final ImmutableList.Builder<AstExpression> conditions = ImmutableList.builder();
  private final ImmutableList.Builder<AstBlock> thenBlocks = ImmutableList.builder();
  private AstBlock elseBlock;

  public AstIfBuilder() {}

  public void location(Location location) {
    this.location = location;
  }

  public void branch(AstExpression condition, AstBlock thenBlock) {
    conditions.add(condition);
    thenBlocks.add(thenBlock);
  }

  public void elseBlock(AstBlock elseBlock) {
    this.elseBlock = elseBlock;
  }

  public AstIf build() {
    return new AstIf(location, conditions.build(), thenBlocks.build(), elseBlock);
  }
}
