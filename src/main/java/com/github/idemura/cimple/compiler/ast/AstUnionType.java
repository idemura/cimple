package com.github.idemura.cimple.compiler.ast;

import com.github.idemura.cimple.compiler.QualifiedName;
import com.google.common.collect.ImmutableList;
import java.util.List;

public final class AstUnionType extends AstType {
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

  @Override
  public void setName(QualifiedName name) {
    this.name = name;
  }

  public List<UnionVariant> getVariants() {
    return variants;
  }

  public void setVariants(List<UnionVariant> variants) {
    this.variants = ImmutableList.copyOf(variants);
  }
}
