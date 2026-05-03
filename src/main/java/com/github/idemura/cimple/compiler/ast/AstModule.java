package com.github.idemura.cimple.compiler.ast;

import java.util.ArrayList;
import java.util.List;

public final class AstModule extends AstNode {
  private final List<AstNode> definitions = new ArrayList<>();
  private String name;

  public AstModule() {
    super();
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public Object accept(AstVisitor visitor) {
    return visitor.visit(this);
  }

  public List<AstNode> definitions() {
    return definitions;
  }
}
