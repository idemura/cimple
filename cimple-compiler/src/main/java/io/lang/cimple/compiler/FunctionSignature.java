package io.lang.cimple.compiler;

import static java.util.stream.Collectors.joining;

import com.google.common.collect.ImmutableList;

public record FunctionSignature(String name, ImmutableList<AstType> parameterTypes) {
  public FunctionSignature(String name, ImmutableList<AstType> parameterTypes) {
    this.name = name;
    this.parameterTypes = ImmutableList.copyOf(parameterTypes);
  }

  @Override
  public String toString() {
    return "%s(%s)"
        .formatted(name, parameterTypes.stream().map(AstType::toString).collect(joining(", ")));
  }
}
