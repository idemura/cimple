package io.lang.cimple.compiler;

import java.util.Objects;

public final class AstUnionVariant extends AstNode {
  private final Identifier tag;
  private final AstTypeHolder type;

  public AstUnionVariant(Identifier tag, AstType type) {
    super(tag.location());
    this.tag = tag;
    this.type = new AstTypeHolder(type);
  }

  @Override
  public void accept(AstVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public void acceptChildren(AstVisitor visitor) {
    type.accept(visitor);
  }

  @Override
  public int hashCode() {
    return tag.hashCode();
  }

  @Override
  public boolean equals(Object object) {
    return this == object
        || (object instanceof AstUnionVariant other && Objects.equals(tag, other.tag));
  }

  public Identifier tag() {
    return tag;
  }

  public AstType valueType() {
    return type.get();
  }
}
