package io.lang.cimple.compiler;

public final class AstStringType extends AstType {
  public static final AstStringType INSTANCE = new AstStringType();

  private final Identifier name = new Identifier("string").builtin();

  private AstStringType() {}

  @Override
  public void accept(AstVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public void acceptChildren(AstVisitor visitor) {}

  @Override
  public Identifier name() {
    return name;
  }
}
