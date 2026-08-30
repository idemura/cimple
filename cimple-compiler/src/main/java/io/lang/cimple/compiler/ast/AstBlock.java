package io.lang.cimple.compiler.ast;

import java.util.ArrayList;
import java.util.List;

public final class AstBlock extends AstNode {
  private final List<AstStatement> statements = new ArrayList<>();

  public AstBlock() {}

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

  public List<AstStatement> statements() {
    return statements;
  }
}
