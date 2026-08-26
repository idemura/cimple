package com.github.idemura.cimple.compiler.semantics;

import com.github.idemura.cimple.compiler.ErrorConsumer;
import com.github.idemura.cimple.compiler.Identifier;
import com.github.idemura.cimple.compiler.ast.AstArrayType;
import com.github.idemura.cimple.compiler.ast.AstBuiltinType;
import com.github.idemura.cimple.compiler.ast.AstNew;
import com.github.idemura.cimple.compiler.ast.AstPointerType;
import com.github.idemura.cimple.compiler.ast.AstType;
import com.github.idemura.cimple.compiler.ast.AstTypeHolder;
import com.github.idemura.cimple.compiler.ast.AstTypeRef;
import com.github.idemura.cimple.compiler.ast.AstVisitor;

public class TypeRefResolutionVisitor extends AstVisitor {
  private final GlobalNameMap globalNameMap;
  private final LocalNameMap localNameMap;
  private final ErrorConsumer errorConsumer;

  public TypeRefResolutionVisitor(
      GlobalNameMap globalNameMap, LocalNameMap localNameMap, ErrorConsumer errorConsumer) {
    this.globalNameMap = globalNameMap;
    this.localNameMap = localNameMap;
    this.errorConsumer = errorConsumer;
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
  protected void visit(AstNew node) {
    super.visit(node);
    node.type(new AstPointerType(resolveTypeRefSafe(node.type())));
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
      return localNameMap.lookupType(name);
    }
    return globalNameMap.lookupType(name);
  }
}
