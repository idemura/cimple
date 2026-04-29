package com.github.idemura.cimple.compiler.ast;

import com.google.common.collect.ImmutableList;
import java.util.List;

public final class AstTypeUnion extends AstType {
  private QualifiedName name;
  private List<UnionVariant> variants;

  @Override
  public Object accept(AstVisitor visitor) {
    return visitor.visit(this);
  }

  @Override
  public QualifiedName getName() {
    return name;
  }

  public void setName(QualifiedName name) {
    this.name = name;
  }

  public void setVariants(List<UnionVariant> variants) {
    this.variants = ImmutableList.copyOf(variants);
  }

  public List<UnionVariant> getVariants() {
    return ImmutableList.copyOf(variants);
  }
}
