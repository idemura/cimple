package io.lang.cimple.compiler.ast;

import io.lang.cimple.compiler.Identifier;

public final class AstTypeRef extends AstType {
  private Identifier name;

  @Override
  public void accept(AstVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public void acceptChildren(AstVisitor visitor) {}

  @Override
  public String toString() {
    return "TYPE_REF(%s)".formatted(name);
  }

  @Override
  public Identifier name() {
    return name;
  }

  @Override
  public void name(Identifier name) {
    this.name = name;
  }
}
