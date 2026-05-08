package com.github.idemura.cimple.compiler.semantics;

import static com.google.common.base.Preconditions.checkState;

import com.github.idemura.cimple.compiler.ErrorConsumer;
import com.github.idemura.cimple.compiler.ast.AstBlock;
import com.github.idemura.cimple.compiler.ast.AstCall;
import com.github.idemura.cimple.compiler.ast.AstCast;
import com.github.idemura.cimple.compiler.ast.AstEntityRef;
import com.github.idemura.cimple.compiler.ast.AstExpression;
import com.github.idemura.cimple.compiler.ast.AstExpressionRewriteVisitor;
import com.github.idemura.cimple.compiler.ast.AstFunction;
import com.github.idemura.cimple.compiler.ast.AstFunctionHeader;
import com.github.idemura.cimple.compiler.ast.AstFunctionType;
import com.github.idemura.cimple.compiler.ast.AstLocal;
import com.github.idemura.cimple.compiler.ast.AstModule;
import com.github.idemura.cimple.compiler.ast.AstRecordType;
import com.github.idemura.cimple.compiler.ast.AstTypeRef;
import com.github.idemura.cimple.compiler.ast.AstUnionType;
import com.github.idemura.cimple.compiler.ast.AstVariable;

public class NameResolutionVisitor extends AstExpressionRewriteVisitor {
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
  protected Object visit(AstFunctionHeader node) {
    return super.visit(node);
  }

  @Override
  protected Object visit(AstFunction node) {
    try {
      resolveHeader(node.header());
      nameMap.beginScope();
      for (var parameter : node.header().parameters()) {
        registerLocal(parameter);
      }
      return super.visit(node);
    } finally {
      nameMap.endScope();
    }
  }

  @Override
  protected Object visit(AstVariable node) {
    resolveTypeRef(node.type());
    return super.visit(node);
  }

  @Override
  protected Object visit(AstTypeRef node) {
    if (node.isNameResolved()) {
      return super.visit(node);
    }
    // TODO: Resolve
    return super.visit(node);
  }

  @Override
  protected Object visit(AstFunctionType node) {
    resolveHeader(node.header());
    return super.visit(node);
  }

  @Override
  protected Object visit(AstRecordType node) {
    for (var field : node.fields()) {
      resolveTypeRef(field.type());
    }
    return super.visit(node);
  }

  @Override
  protected Object visit(AstUnionType node) {
    for (var variant : node.variants()) {
      resolveTypeRef(variant.valueType());
    }
    return super.visit(node);
  }

  @Override
  protected Object visit(AstBlock node) {
    return super.visit(node);
  }

  @Override
  protected Object visit(AstLocal node) {
    registerLocal(node.variable());
    return super.visit(node);
  }

  @Override
  protected Object visit(AstEntityRef node) {
    if (node.isNameResolved()) {
      return node;
    }
    // Do not resolve builtin here. It will be resolved in AstCall.
    if (node.isBuiltin()) {
      return node;
    }
    try {
      var entity = nameMap.lookupEntity(node.name().name());
      if (entity == null) {
        errorConsumer.errorAt(node.location(), "Undefined name: %s", node.name());
        return node;
      }
      node.entity(entity);
      return node;
    } finally {
      node.markNameResolved();
    }
  }

  @Override
  protected Object visit(AstCall node) {
    try {
      // First, resolve children.
      super.visit(node);
      // If this is a builtin method call, resolve overload. Add casts if necessary.
      var function = node.function();
      if (function instanceof AstEntityRef ref && ref.isBuiltin()) {
        checkState(!ref.isNameResolved());
        return resolveBuiltinCall(node);
      }
      // Normal function resolved when AstEntityRef resolved.
      return node;
    } finally {
      node.markNameResolved();
    }
  }

  @Override
  protected Object visit(AstCast node) {
    resolveTypeRef(node.type());
    return super.visit(node);
  }

  private void resolveHeader(AstFunctionHeader header) {
    if (header.receiverType() != null) {
      resolveTypeRef(header.receiverType());
    }
    resolveTypeRef(header.resultType());
  }

  private void resolveTypeRef(AstTypeRef typeRef) {
    try {
      var type = nameMap.lookupType(typeRef.name());
      if (type == null) {
        errorConsumer.errorAt(typeRef.location(), "Undefined type: %s", typeRef.name());
        return;
      }
      typeRef.name(type.name());
      typeRef.type(type);
    } finally {
      typeRef.markNameResolved();
    }
  }

  private AstExpression resolveBuiltinCall(AstCall node) {
    // TODO Resolve using arguments
    var operatorRef = (AstEntityRef) node.function();
    AstFunction function;
    switch (operatorRef.name().name()) {
      case "+":
        {
          function = BuiltinFunctions.ADD_I64_I64;
          break;
        }
      case "*":
        {
          function = BuiltinFunctions.MUL_I64_I64;
          break;
        }
      default:
        throw new IllegalStateException(
            "Unknown builtin entity '%s'".formatted(operatorRef.name()));
    }
    operatorRef.name(function.name());
    operatorRef.entity(function);
    return node;
  }

  private void registerLocal(AstVariable variable) {
    var existing = nameMap.addLocal(variable);
    if (existing != null) {
      errorConsumer.errorAt(
          variable.location(),
          "Duplicate local variable: %s. Defined at %s.",
          variable.name(),
          existing.location());
    }
  }
}
