package io.lang.cimple.compiler;

public final class AstTypeRef extends AstType {
  private final Identifier name;

  public AstTypeRef(Identifier name) {
    super(name.location());
    this.name = name;
  }

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
}
