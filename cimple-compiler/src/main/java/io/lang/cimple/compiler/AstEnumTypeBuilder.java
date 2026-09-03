package io.lang.cimple.compiler;

import com.google.common.collect.ImmutableList;

public final class AstEnumTypeBuilder {
  private Identifier name;
  private AstType baseType;
  private final ImmutableList.Builder<AstEnumVariant> variants = ImmutableList.builder();

  public AstEnumTypeBuilder() {}

  public void name(Identifier name) {
    this.name = name;
  }

  public void baseType(AstType baseType) {
    this.baseType = baseType;
  }

  public void variant(AstEnumVariant variant) {
    variants.add(variant);
  }

  public AstEnumType build() {
    return new AstEnumType(name, baseType, variants.build());
  }
}
