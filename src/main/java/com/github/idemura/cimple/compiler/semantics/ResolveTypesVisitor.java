package com.github.idemura.cimple.compiler.semantics;

import static com.github.idemura.cimple.compiler.ast.AstBuiltinType.isIntegerType;

import com.github.idemura.cimple.compiler.ErrorConsumer;
import com.github.idemura.cimple.compiler.Identifier;
import com.github.idemura.cimple.compiler.ast.AstArrayType;
import com.github.idemura.cimple.compiler.ast.AstBuiltinType;
import com.github.idemura.cimple.compiler.ast.AstEnumType;
import com.github.idemura.cimple.compiler.ast.AstFunction;
import com.github.idemura.cimple.compiler.ast.AstModule;
import com.github.idemura.cimple.compiler.ast.AstNew;
import com.github.idemura.cimple.compiler.ast.AstPointerType;
import com.github.idemura.cimple.compiler.ast.AstType;
import com.github.idemura.cimple.compiler.ast.AstTypeHolder;
import com.github.idemura.cimple.compiler.ast.AstTypeRef;
import com.github.idemura.cimple.compiler.ast.AstVisitor;
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
    typeMap = globalNameMap.collectTypes(node, errorConsumer);
    super.visit(node);
  }

  @Override
  protected void visit(AstFunction node) {
    super.visit(node);
    var objectType = node.header().objectType();
    if (objectType != null) {
      // When we resolved the object type, qualified function name may be affected.
      node.name(objectType.name().withEntity(node.name().entityName()));
    }
  }

  @Override
  protected void visit(AstTypeHolder node) {
    node.set(resolveTypeRefSafe(node.get()));
    // Stop here: after resolution, the holder points at a shared type definition, not an owned
    // child subtree. Walking into it would revisit definitions through references and can recurse
    // forever for valid shapes such as `record T { var next T*; }`.
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
          node.baseType().formatName());
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
      pointerType.baseType(resolveTypeRefSafe(pointerType.baseType()));
    }
    if (type instanceof AstArrayType arrayType) {
      arrayType.baseType(resolveTypeRefSafe(arrayType.baseType()));
    }
    return type;
  }

  private AstType lookupType(Identifier name) {
    if (name.moduleName() == null) {
      var builtinType = GlobalNameMap.lookupBuiltinType(name.typeName());
      if (builtinType != null) {
        return builtinType;
      }
      return typeMap.get(name.typeName());
    }
    return globalNameMap.lookupType(name);
  }
}
