package com.github.idemura.cimple.compiler.semantics;

import com.github.idemura.cimple.compiler.ErrorConsumer;
import com.github.idemura.cimple.compiler.ast.AstBlock;
import com.github.idemura.cimple.compiler.ast.AstCast;
import com.github.idemura.cimple.compiler.ast.AstEntityRef;
import com.github.idemura.cimple.compiler.ast.AstFunction;
import com.github.idemura.cimple.compiler.ast.AstFunctionHeader;
import com.github.idemura.cimple.compiler.ast.AstFunctionType;
import com.github.idemura.cimple.compiler.ast.AstModule;
import com.github.idemura.cimple.compiler.ast.AstRecordType;
import com.github.idemura.cimple.compiler.ast.AstTypeRef;
import com.github.idemura.cimple.compiler.ast.AstUnionType;
import com.github.idemura.cimple.compiler.ast.AstVariable;
import com.github.idemura.cimple.compiler.ast.AstVisitor;

/// Resolves names.
public class NameResolutionVisitor extends AstVisitor {
  private final NameMap nameMap;
  private final ErrorConsumer errorConsumer;

  public NameResolutionVisitor(NameMap nameMap, ErrorConsumer errorConsumer) {
    this.nameMap = nameMap;
    this.errorConsumer = errorConsumer;
  }

  @Override
  protected Object visit(AstModule node) {
    return super.visit(node);
  }

  @Override
  protected Object visit(AstFunction node) {
    resolveHeader(node.getHeader());
    return super.visit(node);
  }

  @Override
  protected Object visit(AstBlock node) {
    return super.visit(node);
  }

  @Override
  protected Object visit(AstVariable node) {
    resolveTypeRef(node.getType());
    return super.visit(node);
  }

  @Override
  protected Object visit(AstFunctionType node) {
    resolveHeader(node.getHeader());
    return super.visit(node);
  }

  @Override
  protected Object visit(AstRecordType node) {
    for (var field : node.getFields()) {
      resolveTypeRef(field.getType());
    }
    return super.visit(node);
  }

  @Override
  protected Object visit(AstUnionType node) {
    for (var variant : node.getVariants()) {
      resolveVariant(variant);
    }
    return super.visit(node);
  }

  @Override
  protected Object visit(AstCast node) {
    resolveTypeRef(node.getTypeRef());
    return super.visit(node);
  }

  @Override
  protected Object visit(AstEntityRef node) {
    if (node.isNameResolved()) {
      return null;
    }
    try {
      var entity = nameMap.lookupEntity(node.getName().name());
      if (entity == null) {
        errorConsumer.errorAt(node.getLocation(), "Undefined name: %s", node.getName());
        return null;
      }
      node.setEntity(entity);
      return null;
    } finally {
      node.markNameResolved();
    }
  }

  private void resolveHeader(AstFunctionHeader header) {
    if (header.getObjectType() != null) {
      resolveTypeRef(header.getObjectType());
    }
    resolveTypeRef(header.getResultType());
  }

  private void resolveVariant(AstUnionType.Variant variant) {
    resolveTypeRef(variant.getValueType());
  }

  private void resolveTypeRef(AstTypeRef typeRef) {
    // var type = nameMap(moduleName, new QualifiedName(typeName));
    // if (type == null) {
    //   errorConsumer.error(location, "Undefined type: %s", typeName);
    // }
  }
}
