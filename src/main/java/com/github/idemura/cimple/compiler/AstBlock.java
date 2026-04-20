package com.github.idemura.cimple.compiler;

import java.util.ArrayList;
import java.util.List;

public class AstBlock extends AstNode {
  private final List<AstStatement> statements = new ArrayList<>();

  AstBlock(Location location) {
    super(location);
  }

  @Override
  public void accept(Visitor visitor) {
    visitor.visit(this);
  }

  void add(AstStatement s) {
    statements.add(s);
  }

  List<AstStatement> getStatements() {
    return List.copyOf(statements);
  }
}
