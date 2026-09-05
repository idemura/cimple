package io.lang.cimple.compiler;

import com.google.common.collect.ImmutableList;

public final class AstUnionType extends AstType {
  private final Identifier name;
  private final ImmutableList<AstUnionVariant> variants;

  public AstUnionType(Identifier name, ImmutableList<AstUnionVariant> variants) {
    super(name.location());
    this.name = name;
    this.variants = variants;
  }

  @Override
  public void accept(AstVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public void acceptChildren(AstVisitor visitor) {
    for (var variant : variants) {
      variant.accept(visitor);
    }
  }

  @Override
  public Identifier name() {
    return name;
  }

  public ImmutableList<AstUnionVariant> variants() {
    return variants;
  }

  public boolean hasPayload() {
    for (var variant : variants) {
      if (variant.valueType() != null) {
        return true;
      }
    }
    return false;
  }
}
