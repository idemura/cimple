package io.lang.cimple.compiler;

import static io.lang.cimple.compiler.AstBuiltinType.isIntegerType;

import java.util.Map;

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
  protected void visit(AstEnumType node) {
    super.visit(node);
    if (node.baseType() == null) {
      node.baseType(AstBuiltinType.INT64);
    }
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
}
