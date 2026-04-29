package com.github.idemura.cimple.compiler.ast;

import java.util.ArrayList;
import java.util.List;

public final class AstTypeUnion extends AstType {
  private List<UnionVariant> variants = new ArrayList<>();

  @Override
  public Object accept(AstVisitor visitor) {
    return visitor.visit(this);
  }

  public void addVariant(UnionVariant variant) {
    variants.add(variant);
  }

  public List<UnionVariant> getVariants() {
    return List.copyOf(variants);
  }
}
