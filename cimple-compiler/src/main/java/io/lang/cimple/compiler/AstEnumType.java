package io.lang.cimple.compiler;

import com.google.common.collect.ImmutableList;

public final class AstEnumType extends AstType {
  private final Identifier name;
  private final AstTypeHolder baseType;
  private final ImmutableList<AstEnumVariant> variants;

  public AstEnumType(Identifier name, AstType baseType, ImmutableList<AstEnumVariant> variants) {
    this.name = name;
    this.baseType = new AstTypeHolder(baseType);
    this.variants = variants;
  }

  @Override
  public void accept(AstVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public void acceptChildren(AstVisitor visitor) {
    acceptSafe(baseType, visitor);
    for (var variant : variants) {
      variant.accept(visitor);
    }
  }

  @Override
  public Identifier name() {
    return name;
  }

  public AstType baseType() {
    return baseType.get();
  }

  public void baseType(AstType baseType) {
    this.baseType.set(baseType);
  }

  public ImmutableList<AstEnumVariant> variants() {
    return variants;
  }
}
