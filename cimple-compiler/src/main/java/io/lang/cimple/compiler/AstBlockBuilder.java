package io.lang.cimple.compiler;

import com.google.common.collect.ImmutableList;

public final class AstBlockBuilder {
  private final ImmutableList.Builder<AstStatement> statements = ImmutableList.builder();

  public AstBlockBuilder() {}

  public void statement(AstStatement statement) {
    statements.add(statement);
  }

  public AstBlock build() {
    return new AstBlock(statements.build());
  }
}
