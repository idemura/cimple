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

  protected Object visit(AstFunctionHeader node) {
    node.receiverType(resolveTypeRefSafe(node.receiverType()));
    node.resultType(resolveTypeRefSafe(node.resultType()));
    return super.visit(node);
  }

  protected Object visit(AstFunction node) {
    node.type(resolveTypeRefSafe(node.type()));
    return super.visit(node);
  }

  protected Object visit(AstVariable node) {
    node.type(resolveTypeRefSafe(node.type()));
    return super.visit(node);
  }

  @Override
  protected Object visit(AstTypeRef node) {
    throw new IllegalStateException("AstTypeRef must be replaced at this point");
  }

  protected Object visit(AstUnionType node) {
    for (var variant : node.variants()) {
      variant.valueType(resolveTypeRefSafe(variant.valueType()));
    }
    return super.visit(node);
  }

  protected Object visit(AstNullLiteral node) {
    node.type(resolveTypeRefSafe(node.type()));
    return super.visit(node);
  }

  protected Object visit(AstBoolLiteral node) {
    node.type(resolveTypeRefSafe(node.type()));
    return super.visit(node);
  }

  protected Object visit(AstNumberLiteral node) {
    node.type(resolveTypeRefSafe(node.type()));
    return super.visit(node);
  }

  protected Object visit(AstStringLiteral node) {
    node.type(resolveTypeRefSafe(node.type()));
    return super.visit(node);
  }

  protected Object visit(AstNew node) {
    node.type(resolveTypeRefSafe(node.type()));
    return super.visit(node);
  }

  protected Object visit(AstCast node) {
    node.type(resolveTypeRefSafe(node.type()));
    return super.visit(node);
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
    return type;
  }
}
