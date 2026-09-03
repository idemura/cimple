package io.lang.cimple.compiler;

import com.google.common.collect.ImmutableList;

public final class AstModuleBuilder {
  private Identifier name;
  private final ImmutableList.Builder<AstNode> definitions = ImmutableList.builder();

  public AstModuleBuilder() {}

  public void name(Identifier name) {
    this.name = name;
  }

  public void definition(AstNode definition) {
    definitions.add(definition);
  }

  public AstModule build() {
    return new AstModule(name, definitions.build());
  }
}
