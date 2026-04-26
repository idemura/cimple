package com.github.idemura.cimple.compiler;

import java.util.ArrayList;
import java.util.List;

public class AstBlock extends AstAbstractNode {
  private List<AstAbstractStatement> statements = new ArrayList<>();

  AstBlock() {}

  @Override
  public void accept(Visitor visitor) {
    visitor.visit(this);
  }

  void add(AstAbstractStatement s) {
    statements.add(s);
  }

  List<AstAbstractStatement> getStatements() {
    return List.copyOf(statements);
  }
}
