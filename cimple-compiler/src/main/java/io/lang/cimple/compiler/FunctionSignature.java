package io.lang.cimple.compiler;

import static com.google.common.collect.ImmutableList.toImmutableList;

import com.google.common.collect.ImmutableList;
import io.lang.cimple.compiler.ast.AstFunction;
import io.lang.cimple.compiler.ast.AstType;
import io.lang.cimple.compiler.ast.AstVariable;
import java.util.List;
import java.util.Objects;

public final class FunctionSignature {
  private final Identifier name;
  private final List<AstType> parameterTypes;

  public FunctionSignature(Identifier name, List<AstType> parameterTypes) {
    this.name = name;
    this.parameterTypes = ImmutableList.copyOf(parameterTypes);
  }

  public static FunctionSignature of(AstFunction function) {
    return new FunctionSignature(
        function.name(),
        function.header().parameters().stream().map(AstVariable::type).collect(toImmutableList()));
  }

  public Identifier name() {
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
    return "%s(%s)".formatted(name, parameterTypes);
  }
}
