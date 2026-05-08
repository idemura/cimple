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
      resolveHeader(node.getHeader());
      nameMap.beginScope();
      for (var parameter : node.getHeader().getParameters()) {
        registerLocal(parameter);
      }
      return super.visit(node);
    } finally {
      nameMap.endScope();
    }
  }

  @Override
  protected Object visit(AstVariable node) {
    resolveTypeRef(node.getType());
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
      resolveTypeRef(variant.getValueType());
    }
    return super.visit(node);
  }

  @Override
  protected Object visit(AstBlock node) {
    return super.visit(node);
  }

  @Override
  protected Object visit(AstLocal node) {
    registerLocal(node.getVariable());
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
      var entity = nameMap.lookupEntity(node.getName().name());
      if (entity == null) {
        errorConsumer.errorAt(node.getLocation(), "Undefined name: %s", node.getName());
        return node;
      }
      node.setEntity(entity);
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
      var function = node.getFunction();
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
    resolveTypeRef(node.getTypeRef());
    return super.visit(node);
  }

  private void resolveHeader(AstFunctionHeader header) {
    if (header.getReceiverType() != null) {
      resolveTypeRef(header.getReceiverType());
    }
    resolveTypeRef(header.getResultType());
  }

  private void resolveTypeRef(AstTypeRef typeRef) {
    try {
      var type = nameMap.lookupType(typeRef.getName());
      if (type == null) {
        errorConsumer.errorAt(typeRef.getLocation(), "Undefined type: %s", typeRef.getName());
        return;
      }
      typeRef.setName(type.getName());
      typeRef.setType(type);
    } finally {
      typeRef.markNameResolved();
    }
  }

  private AstExpression resolveBuiltinCall(AstCall node) {
    // TODO Resolve using arguments
    var operatorRef = (AstEntityRef) node.getFunction();
    AstFunction function;
    switch (operatorRef.getName().name()) {
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
            "Unknown builtin entity '%s'".formatted(operatorRef.getName()));
    }
    operatorRef.setName(function.getName());
    operatorRef.setEntity(function);
    return node;
  }

  private void registerLocal(AstVariable variable) {
    var existing = nameMap.addLocal(variable);
    if (existing != null) {
      errorConsumer.errorAt(
          variable.getLocation(),
          "Duplicate local variable: %s. Defined at %s.",
          variable.getName(),
          existing.getLocation());
    }
  }
}
