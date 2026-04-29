package com.github.idemura.cimple.compiler.ast;

import java.util.ArrayList;
import java.util.List;

public final class AstBlock extends AstNode {
  private List<AstStatement> statements = new ArrayList<>();

  public AstBlock() {}

  @Override
  public Object accept(AstVisitor visitor) {
    return visitor.visit(this);
  }

  public List<AstStatement> statements() {
    return statements;
  }
}
