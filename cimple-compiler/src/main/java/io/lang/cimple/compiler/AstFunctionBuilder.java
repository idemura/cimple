package io.lang.cimple.compiler;

import com.google.common.collect.ImmutableList;

public final class AstFunctionBuilder {
  private Identifier name;
  private final ImmutableList.Builder<AstTypeWildcard> wildcards = ImmutableList.builder();
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

  public void wildcard(AstTypeWildcard wildcard) {
    wildcards.add(wildcard);
  }

  public void resultType(AstType resultType) {
    this.resultType = resultType;
  }

  public void block(AstBlock block) {
    this.block = block;
  }

  public AstFunction build() {
    return new AstFunction(name, wildcards.build(), parameters.build(), resultType, block);
  }
}
