package io.lang.cimple.compiler;

import com.google.common.collect.ImmutableList;

public final class AstInterfaceTypeBuilder {
  private Identifier name;
  private final ImmutableList.Builder<AstFunction> functions = ImmutableList.builder();

  public AstInterfaceTypeBuilder() {}

  public void name(Identifier name) {
    this.name = name;
  }

  public void function(AstFunction function) {
    functions.add(function);
  }

  public AstInterfaceType build() {
    return new AstInterfaceType(name, functions.build());
  }
}
