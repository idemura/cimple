package io.lang.cimple.compiler;

import java.util.Objects;

public final class AstUnionVariant {
  private final Identifier tag;
  private final AstTypeHolder type;

  public AstUnionVariant(Identifier tag, AstType type) {
    this.tag = tag;
    this.type = new AstTypeHolder(type);
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

  AstTypeHolder typeHolder() {
    return type;
  }
}
