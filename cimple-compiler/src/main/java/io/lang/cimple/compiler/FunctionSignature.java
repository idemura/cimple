package io.lang.cimple.compiler;

import static java.util.stream.Collectors.joining;

import com.google.common.collect.ImmutableList;
import io.lang.cimple.compiler.ast.AstType;
import java.util.List;
import java.util.Objects;

public final class FunctionSignature {
  private final String name;
  private final List<AstType> parameterTypes;

  public FunctionSignature(String name, List<AstType> parameterTypes) {
    this.name = name;
    this.parameterTypes = ImmutableList.copyOf(parameterTypes);
  }

  public String name() {
    return name;
  }

  public List<AstType> parameterTypes() {
    return parameterTypes;
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, parameterTypes);
  }

  @Override
  public boolean equals(Object object) {
    return this == object
        || (object instanceof FunctionSignature other
            && Objects.equals(name, other.name)
            && Objects.equals(parameterTypes, other.parameterTypes));
  }

  @Override
  public String toString() {
    return "%s(%s)"
        .formatted(name, parameterTypes.stream().map(AstType::formatName).collect(joining(", ")));
  }
}
