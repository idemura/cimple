package io.lang.cimple.compiler;

import com.google.common.collect.ImmutableList;

public final class AstUnionTypeBuilder {
  private Identifier name;
  private final ImmutableList.Builder<AstUnionVariant> variants = ImmutableList.builder();

  public AstUnionTypeBuilder() {}

  public void name(Identifier name) {
    this.name = name;
  }

  public void variant(AstUnionVariant variant) {
    variants.add(variant);
  }

  public AstUnionType build() {
    return new AstUnionType(name, variants.build());
  }
}
