package io.lang.cimple.compiler;

import com.google.common.collect.ImmutableList;

public final class AstStructTypeBuilder {
  private Identifier name;
  private final ImmutableList.Builder<AstVariable> fields = ImmutableList.builder();

  public AstStructTypeBuilder() {}

  public void name(Identifier name) {
    this.name = name;
  }

  public void field(AstVariable field) {
    fields.add(field);
  }

  public AstStructType build() {
    return new AstStructType(name, fields.build());
  }
}
