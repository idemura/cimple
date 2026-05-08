package com.github.idemura.cimple.compiler.semantics;

import static com.google.common.base.Preconditions.checkState;

import com.github.idemura.cimple.compiler.ErrorConsumer;
import com.github.idemura.cimple.compiler.ast.AstBlock;
import com.github.idemura.cimple.compiler.ast.AstBuiltinType;
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
import com.github.idemura.cimple.compiler.ast.AstReceiverLookup;
import com.github.idemura.cimple.compiler.ast.AstRecordType;
import com.github.idemura.cimple.compiler.ast.AstTypeRef;
import com.github.idemura.cimple.compiler.ast.AstUnionType;
import com.github.idemura.cimple.compiler.ast.AstVariable;
import java.util.ArrayList;

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
    resolveTypeRef(node.typeRef());
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
      resolveTypeRef(field.typeRef());
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
    // Builtin names are resolved in AstCall, after the argument expressions are available.
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
      // Resolve the callee expression and all argument expressions first.
      super.visit(node);
      var function = node.function();
      if (function instanceof AstReceiverLookup receiverLookup) {
        return resolveReceiverCall(node, receiverLookup);
      }
      // Builtin calls are selected here, once argument expressions are available.
      if (function instanceof AstEntityRef ref && ref.isBuiltin()) {
        checkState(!ref.isNameResolved());
        return resolveBuiltinCall(node);
      }
      // Normal function resolved when AstEntityRef resolved.
      // Receiver lookup and builtin resolution may replace the callee expression.
      return node;
    } finally {
      node.markNameResolved();
    }
  }

  @Override
  protected Object visit(AstCast node) {
    resolveTypeRef(node.typeRef());
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
    // TODO: Select the builtin overload using the resolved argument types.
    var operatorRef = (AstEntityRef) node.function();
    AstFunction function;
    switch (operatorRef.name().name()) {
      case "+":
        {
          function = BuiltinFunctions.ADD_I64;
          break;
        }
      case "*":
        {
          function = BuiltinFunctions.MUL_I64;
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

  private AstExpression resolveReceiverCall(AstCall node, AstReceiverLookup receiverLookup) {
    var receiverType = receiverLookup.receiver().typeRef();
    checkState(receiverType != null);
    if (receiverType.type() == AstBuiltinType.NULL) {
      errorConsumer.errorAt(
          receiverLookup.location(),
          "Cannot resolve receiver function %s for null receiver",
          receiverLookup.functionName());
      return node;
    }
    var function = nameMap.lookupReceiverFunction(receiverType.name(), receiverLookup.functionName());
    if (function == null) {
      errorConsumer.errorAt(
          receiverLookup.location(),
          "Undefined receiver function: %s:%s",
          receiverType.name(),
          receiverLookup.functionName());
      return node;
    }

    var functionRef = new AstEntityRef();
    functionRef.name(function.name());
    functionRef.entity(function);
    functionRef.markNameResolved();
    functionRef.location(receiverLookup.location());

    var arguments = new ArrayList<>(node.arguments());
    arguments.add(function.header().receiverIndex(), receiverLookup.receiver());
    node.arguments(arguments);
    node.function(functionRef);
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
