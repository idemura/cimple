package io.lang.cimple.compiler;

import static com.google.common.collect.ImmutableList.toImmutableList;

import com.google.common.collect.ImmutableList;

public final class AstIf extends AstStatement {
  private final ImmutableList<AstExpressionHolder> conditions;
  private final ImmutableList<AstBlock> thenBlocks;
  private final AstBlock elseBlock;

  public AstIf(
      Location location,
      ImmutableList<AstExpression> conditions,
      ImmutableList<AstBlock> thenBlocks,
      AstBlock elseBlock) {
    super(location);
    this.conditions = conditions.stream().map(AstExpressionHolder::new).collect(toImmutableList());
    this.thenBlocks = thenBlocks;
    this.elseBlock = elseBlock;
  }

  @Override
  public void accept(AstVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public void acceptChildren(AstVisitor visitor) {
    for (var condition : conditions) {
      condition.accept(visitor);
    }
    for (var thenBlock : thenBlocks) {
      thenBlock.accept(visitor);
    }
    acceptSafe(elseBlock, visitor);
  }

  public ImmutableList<AstExpression> conditions() {
    return conditions.stream().map(AstExpressionHolder::get).collect(toImmutableList());
  }

  public ImmutableList<AstBlock> thenBlocks() {
    return thenBlocks;
  }

  public AstBlock elseBlock() {
    return elseBlock;
  }
}
