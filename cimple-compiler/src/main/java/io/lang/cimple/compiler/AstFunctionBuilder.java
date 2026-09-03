package io.lang.cimple.compiler;

import com.google.common.collect.ImmutableList;

public final class AstFunctionBuilder {
  private Identifier name;
  private final ImmutableList.Builder<AstVariable> parameters = ImmutableList.builder();
  private AstType resultType;
  private AstBlock block;

  public AstFunctionBuilder() {}

  public void name(Identifier name) {
    this.name = name;
  }

  public void parameter(AstVariable parameter) {
    parameters.add(parameter);
  }

  public void resultType(AstType resultType) {
    this.resultType = resultType;
  }

  public void block(AstBlock block) {
    this.block = block;
  }

  public AstFunction build() {
    return new AstFunction(name, parameters.build(), resultType, block);
  }
}
