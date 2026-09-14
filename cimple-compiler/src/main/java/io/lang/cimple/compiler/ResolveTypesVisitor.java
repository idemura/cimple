package io.lang.cimple.compiler;

import static io.lang.cimple.compiler.AstBuiltinType.isIntegerType;

import com.google.common.collect.ImmutableList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ResolveTypesVisitor extends AstVisitor {
  private final GlobalNameMap globalNameMap;
  private final ErrorConsumer errorConsumer;
  private Map<String, AstType> typeMap;

  public ResolveTypesVisitor(GlobalNameMap globalNameMap, ErrorConsumer errorConsumer) {
    this.globalNameMap = globalNameMap;
    this.errorConsumer = errorConsumer;
  }

  @Override
  protected void visit(AstModule node) {
    // TODO: Include import names.
    typeMap = globalNameMap.collectTypes(node.name().entity(), errorConsumer);
    super.visit(node);
  }

  @Override
  protected void visit(AstTypeHolder node) {
    node.set(resolveTypeRefSafe(node.get()));
    // Stop here: after resolution, the holder points at a shared type definition, not an owned
    // child subtree. Walking into it would revisit definitions through references and can recurse
    // forever for valid shapes such as `struct T { var next T*; }`.
  }

  @Override
  protected void visit(AstTypeRef node) {}

  @Override
  protected void visit(AstFunction node) {
    var shadowed = putIntoTypeMap(node.wildcards());
    try {
      super.visit(node);
      checkGenericParametersUsedInParameterTypes(node);
    } finally {
      removeFromTypeMap(node.wildcards());
      putIntoTypeMap(shadowed);
    }
  }

  @Override
  protected void visit(AstEnumType node) {
    if (node.baseType() == null) {
      node.baseType(AstBuiltinType.INT64);
    }
    super.visit(node);
    if (!isIntegerType(node.baseType())) {
      errorConsumer.errorAt(
          node.location(),
          "Enum '%s' base type must be an integer type, got '%s'",
          node.name(),
          node.baseType());
    }
  }

  @Override
  protected void visit(AstNew node) {
    super.visit(node);
  }

  private AstType resolveTypeRefSafe(AstType type) {
    if (type instanceof AstTypeRef typeRef) {
      var resolvedType = lookupType(typeRef.name());
      if (resolvedType == null) {
        errorConsumer.errorAt(type.location(), "Undefined type: '%s'", type.name());
        return AstBuiltinType.VOID;
      }
      return resolvedType;
    }
    if (type instanceof AstPointerType pointerType) {
      pointerType.resolve(this::resolveTypeRefSafe);
      if (pointerType.containsWildcard()) {
        throw new UnsupportedOperationException("Generic pointer types are not supported yet");
      }
    }
    if (type instanceof AstArrayType arrayType) {
      arrayType.resolve(this::resolveTypeRefSafe);
    }
    return type;
  }

  private AstType lookupType(Identifier name) {
    if (name.module() == null) {
      var builtinType = GlobalNameMap.lookupBuiltinType(name.entity());
      if (builtinType != null) {
        return builtinType;
      }
      return typeMap.get(name.entity());
    }
    return globalNameMap.lookupType(name);
  }

  private List<AstType> putIntoTypeMap(List<? extends AstType> types) {
    var replaced = new ImmutableList.Builder<AstType>();
    for (var type : types) {
      var previous = typeMap.put(type.name().entity(), type);
      if (previous != null) {
        replaced.add(previous);
      }
    }
    return replaced.build();
  }

  private void removeFromTypeMap(List<? extends AstType> types) {
    for (var type : types) {
      typeMap.remove(type.name().entity());
    }
  }

  private void checkGenericParametersUsedInParameterTypes(AstFunction function) {
    var used = new HashSet<AstTypeWildcard>();
    for (var parameter : function.parameters()) {
      collectWildcards(parameter.type(), used);
    }
    for (var wildcard : function.wildcards()) {
      if (!used.contains(wildcard)) {
        errorConsumer.errorAt(
            wildcard.location(),
            "Generic parameter '%s' must be used in function parameter types",
            wildcard.name());
      }
    }
  }

  private static void collectWildcards(AstType type, Set<AstTypeWildcard> result) {
    switch (type) {
      case null -> {}
      case AstTypeWildcard wildcard -> result.add(wildcard);
      case AstArrayType arrayType -> collectWildcards(arrayType.baseType(), result);
      case AstPointerType pointerType -> {
        if (pointerType.containsWildcard()) {
          throw new UnsupportedOperationException("Generic pointer types are not supported yet");
        }
      }
      default -> {}
    }
  }
}
