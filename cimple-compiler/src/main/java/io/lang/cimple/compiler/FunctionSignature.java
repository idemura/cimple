package io.lang.cimple.compiler;

import com.google.common.collect.ImmutableList;

public record FunctionSignature(String name, ImmutableList<AstType> parameterTypes) {
  public FunctionSignature(String name, ImmutableList<AstType> parameterTypes) {
    this.name = name;
    this.parameterTypes = ImmutableList.copyOf(parameterTypes);
  }
}
