package com.github.idemura.cimple.compiler.semantics;

import com.github.idemura.cimple.compiler.ErrorConsumer;
import com.github.idemura.cimple.compiler.ast.AstBlock;
import com.github.idemura.cimple.compiler.ast.AstCall;
import com.github.idemura.cimple.compiler.ast.AstCast;
import com.github.idemura.cimple.compiler.ast.AstEntity;
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
import com.github.idemura.cimple.compiler.ast.UnionVariant;

/// Resolves names.
public class NameResolutionVisitor extends AstVisitor {
  private final NameMap nameMap;
  private final ErrorConsumer errorConsumer;
  private final ScopeNameMap entities = new ScopeNameMap();
  private String moduleName;

  public NameResolutionVisitor(NameMap nameMap, ErrorConsumer errorConsumer) {
    this.nameMap = nameMap;
    this.errorConsumer = errorConsumer;
  }

  @Override
  protected Object visit(AstModule node) {
    moduleName = node.getName();
    for (var definition : node.definitions()) {
      if (definition instanceof AstEntity entity) {
        entities.putGlobal(entity);
      }
    }
    return super.visit(node);
  }

  @Override
  protected Object visit(AstFunction node) {
    resolveHeader(node.getHeader());
    entities.pushScope();
    for (var parameter : node.getHeader().getParameters()) {
      resolveTypeRef(parameter.getType());
      entities.put(parameter);
    }
    visitChildren(node);
    entities.popScope();
    return null;
  }

  @Override
  protected Object visit(AstBlock node) {
    entities.pushScope();
    visitChildren(node);
    entities.popScope();
    return null;
  }

  @Override
  protected Object visit(AstVariable node) {
    resolveTypeRef(node.getType());
    visitChildren(node);
    if (node.getName().moduleName() == null) {
      entities.put(node);
    }
    return null;
  }

  @Override
  protected Object visit(AstFunctionType node) {
    resolveHeader(node.getHeader());
    return null;
  }

  @Override
  protected Object visit(AstRecordType node) {
    for (var field : node.getFields()) {
      resolveTypeRef(field.getType());
    }
    return null;
  }

  @Override
  protected Object visit(AstUnionType node) {
    for (var variant : node.getVariants()) {
      resolveVariant(variant);
    }
    return null;
  }

  @Override
  protected Object visit(AstCast node) {
    resolveTypeRef(node.getTypeRef());
    return super.visit(node);
  }

  @Override
  protected Object visit(AstEntityRef name) {
    var entity = entities.get(name.getName());
    if (entity == null) {
      entity = nameMap.getEntity(name.getName());
    }
    if (entity == null) {
      errorConsumer.error(name.getLocation(), "Undefined name: %s", name.getName());
      return null;
    }
    name.setEntity(entity);
    return null;
  }

  @Override
  protected Object visit(AstCall node) {
    return super.visit(node);
  }

  private void resolveHeader(AstFunctionHeader header) {
    if (header.getObjectType() != null) {
      resolveTypeRef(header.getObjectType());
    }
    resolveTypeRef(header.getResultType());
  }

  private void resolveVariant(UnionVariant variant) {
    resolveTypeRef(variant.getValueType());
  }

  private void resolveTypeRef(AstTypeRef typeRef) {
    // var type = nameMap(moduleName, new QualifiedName(typeName));
    // if (type == null) {
    //   errorConsumer.error(location, "Undefined type: %s", typeName);
    // }
  }
}
