package com.github.idemura.cimple.compiler.semantics;

import com.github.idemura.cimple.compiler.ErrorConsumer;
import com.github.idemura.cimple.compiler.ast.AstBoolLiteral;
import com.github.idemura.cimple.compiler.ast.AstBuiltinType;
import com.github.idemura.cimple.compiler.ast.AstCast;
import com.github.idemura.cimple.compiler.ast.AstFunction;
import com.github.idemura.cimple.compiler.ast.AstFunctionHeader;
import com.github.idemura.cimple.compiler.ast.AstNew;
import com.github.idemura.cimple.compiler.ast.AstNullLiteral;
import com.github.idemura.cimple.compiler.ast.AstNumberLiteral;
import com.github.idemura.cimple.compiler.ast.AstPointerType;
import com.github.idemura.cimple.compiler.ast.AstStringLiteral;
import com.github.idemura.cimple.compiler.ast.AstType;
import com.github.idemura.cimple.compiler.ast.AstTypeRef;
import com.github.idemura.cimple.compiler.ast.AstUnionType;
import com.github.idemura.cimple.compiler.ast.AstVariable;
import com.github.idemura.cimple.compiler.ast.AstVisitor;

public class TypeResolutionVisitor extends AstVisitor {
  private final NameMap nameMap;
  private final ErrorConsumer errorConsumer;

  public TypeResolutionVisitor(NameMap nameMap, ErrorConsumer errorConsumer) {
    this.nameMap = nameMap;
    this.errorConsumer = errorConsumer;
  }

  @Override
  protected void visit(AstFunctionHeader node) {
    super.visit(node);
    node.receiverType(resolveTypeRefSafe(node.receiverType()));
    node.resultType(resolveTypeRefSafe(node.resultType()));
  }

  @Override
  protected void visit(AstFunction node) {
    super.visit(node);
    node.type(resolveTypeRefSafe(node.type()));
  }

  @Override
  protected void visit(AstVariable node) {
    super.visit(node);
    node.type(resolveTypeRefSafe(node.type()));
  }

  @Override
  protected void visit(AstTypeRef node) {
    // throw new IllegalStateException("AstTypeRef must be replaced at this point");
    return;
  }

  @Override
  protected void visit(AstPointerType node) {
    super.visit(node);
    node.baseType(resolveTypeRefSafe(node.baseType()));
  }

  @Override
  protected void visit(AstUnionType node) {
    super.visit(node);
    for (var variant : node.variants()) {
      variant.valueType(resolveTypeRefSafe(variant.valueType()));
    }
  }

  @Override
  protected void visit(AstNullLiteral node) {
    super.visit(node);
    node.type(resolveTypeRefSafe(node.type()));
  }

  @Override
  protected void visit(AstBoolLiteral node) {
    super.visit(node);
    node.type(resolveTypeRefSafe(node.type()));
  }

  @Override
  protected void visit(AstNumberLiteral node) {
    super.visit(node);
    node.type(resolveTypeRefSafe(node.type()));
  }

  @Override
  protected void visit(AstStringLiteral node) {
    super.visit(node);
    node.type(resolveTypeRefSafe(node.type()));
  }

  @Override
  protected void visit(AstNew node) {
    super.visit(node);
    node.type(new AstPointerType(resolveTypeRefSafe(node.type())));
  }

  @Override
  protected void visit(AstCast node) {
    super.visit(node);
    node.type(resolveTypeRefSafe(node.type()));
  }

  private AstType resolveTypeRefSafe(AstType type) {
    if (type instanceof AstTypeRef typeRef) {
      var resolvedType = nameMap.lookupType(typeRef.name());
      if (resolvedType == null) {
        errorConsumer.errorAt(type.location(), "Undefined type: '%s'", type.name());
        return AstBuiltinType.VOID;
      }
      return resolvedType;
    }
    if (type instanceof AstPointerType pointerType) {
      pointerType.baseType(resolveTypeRefSafe(pointerType.baseType()));
    }
    return type;
  }
}
