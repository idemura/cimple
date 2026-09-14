package io.lang.cimple.compiler;

import static java.util.stream.Collectors.joining;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public record FunctionSignature(String name, ImmutableList<AstType> parameterTypes) {
  public FunctionSignature(String name, ImmutableList<AstType> parameterTypes) {
    this.name = name;
    this.parameterTypes = ImmutableList.copyOf(parameterTypes);
  }

  public ImmutableMap<AstTypeWildcard, AstType> match(FunctionSignature actual) {
    if (!name.equals(actual.name) || parameterTypes.size() != actual.parameterTypes.size()) {
      return null;
    }
    var substitutions = new LinkedHashMap<AstTypeWildcard, AstType>();
    for (var i = 0; i < parameterTypes.size(); i++) {
      if (!matchType(parameterTypes.get(i), actual.parameterTypes.get(i), substitutions)) {
        return null;
      }
    }
    return ImmutableMap.copyOf(substitutions);
  }

  @Override
  public int hashCode() {
    // Parameter types cannot contribute because a wildcard is compatible with every type.
    return Objects.hash(name, parameterTypes.size());
  }

  @Override
  public boolean equals(Object object) {
    return this == object
        || (object instanceof FunctionSignature other
            && name.equals(other.name)
            && parameterTypes.size() == other.parameterTypes.size()
            && parameterTypesEqual(other));
  }

  @Override
  public String toString() {
    return "%s(%s)"
        .formatted(name, parameterTypes.stream().map(AstType::toString).collect(joining(", ")));
  }

  private boolean parameterTypesEqual(FunctionSignature other) {
    for (var i = 0; i < parameterTypes.size(); i++) {
      if (!typesEqual(parameterTypes.get(i), other.parameterTypes.get(i))) {
        return false;
      }
    }
    return true;
  }

  private static boolean matchType(
      AstType pattern, AstType actual, Map<AstTypeWildcard, AstType> substitutions) {
    if (containsWildcardUnderPointer(pattern) || containsWildcardUnderPointer(actual)) {
      throw new UnsupportedOperationException("Generic pointer types are not supported yet");
    }
    if (pattern instanceof AstTypeWildcard wildcard) {
      var existing = substitutions.putIfAbsent(wildcard, actual);
      return existing == null || existing.equals(actual);
    }
    if (pattern instanceof AstArrayType patternArray && actual instanceof AstArrayType actualArray) {
      return matchType(patternArray.baseType(), actualArray.baseType(), substitutions);
    }
    return typesEqual(pattern, actual);
  }

  private static boolean typesEqual(AstType left, AstType right) {
    if (left == right) {
      return true;
    }
    if (left == null || right == null) {
      return false;
    }
    if (left instanceof AstTypeWildcard || right instanceof AstTypeWildcard) {
      return true;
    }
    if (left.getClass() != right.getClass()) {
      return false;
    }
    return switch (left) {
      case AstArrayType arrayType ->
          typesEqual(arrayType.baseType(), ((AstArrayType) right).baseType());
      case AstPointerType pointerType ->
          typesEqual(pointerType.baseType(), ((AstPointerType) right).baseType());
      case AstFunctionType functionType -> {
        var otherFunction = ((AstFunctionType) right).function();
        yield functionType.function().signature().equals(otherFunction.signature())
            && typesEqual(functionType.function().resultType(), otherFunction.resultType());
      }
      default -> left.name().equals(right.name());
    };
  }

  private static boolean containsWildcardUnderPointer(AstType type) {
    return switch (type) {
      case AstPointerType pointerType -> pointerType.containsWildcard();
      case AstArrayType arrayType -> containsWildcardUnderPointer(arrayType.baseType());
      default -> false;
    };
  }
}
