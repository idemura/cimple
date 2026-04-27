package com.github.idemura.cimple.compiler.ast;

import java.util.ArrayList;
import java.util.List;

public final class AstBlock extends AstNode {
  private List<AstStatement> statements = new ArrayList<>();

  public AstBlock() {}

  @Override
  public void accept(AstVisitor visitor) {
    visitor.visit(this);
  }

  public void add(AstStatement s) {
    statements.add(s);
  }

  public List<AstStatement> getStatements() {
    return List.copyOf(statements);
  }
}
