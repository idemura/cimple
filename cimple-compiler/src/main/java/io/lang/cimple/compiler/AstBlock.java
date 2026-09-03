package io.lang.cimple.compiler;

import com.google.common.collect.ImmutableList;

public final class AstBlock extends AstNode {
  private final ImmutableList<AstStatement> statements;

  public AstBlock(ImmutableList<AstStatement> statements) {
    this.statements = statements;
  }

  @Override
  public void accept(AstVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public void acceptChildren(AstVisitor visitor) {
    for (var statement : statements) {
      statement.accept(visitor);
    }
  }

  public ImmutableList<AstStatement> statements() {
    return statements;
  }
}
