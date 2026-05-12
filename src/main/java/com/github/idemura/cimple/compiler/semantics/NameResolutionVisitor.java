package com.github.idemura.cimple.compiler.semantics;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.github.idemura.cimple.compiler.ErrorConsumer;
import com.github.idemura.cimple.compiler.ast.AstBlock;
import com.github.idemura.cimple.compiler.ast.AstBuiltinType;
import com.github.idemura.cimple.compiler.ast.AstCall;
import com.github.idemura.cimple.compiler.ast.AstDelete;
import com.github.idemura.cimple.compiler.ast.AstEntityRef;
import com.github.idemura.cimple.compiler.ast.AstExpression;
import com.github.idemura.cimple.compiler.ast.AstExpressionRewriteVisitor;
import com.github.idemura.cimple.compiler.ast.AstExpressionRewriter;
import com.github.idemura.cimple.compiler.ast.AstFieldAccess;
import com.github.idemura.cimple.compiler.ast.AstFunction;
import com.github.idemura.cimple.compiler.ast.AstFunctionHeader;
import com.github.idemura.cimple.compiler.ast.AstFunctionType;
import com.github.idemura.cimple.compiler.ast.AstLocal;
import com.github.idemura.cimple.compiler.ast.AstModule;
import com.github.idemura.cimple.compiler.ast.AstPointerType;
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
    super(new ExpressionRewriter(nameMap, errorConsumer));
    this.nameMap = nameMap;
    this.errorConsumer = errorConsumer;
  }

  @Override
  protected void visit(AstModule node) {
    // Named function types are structural:
    //   type function foo(int x) bool;
    //   type function bar(int x) bool;
    // Values of types `foo` and `bar` are assignment-compatible because the signatures are the
    // same. Functions therefore need an explicit lambda type derived from their headers. Assign
    // those types to all module-level functions before resolving names inside function bodies.
    for (var entity : node.definitions()) {
      if (entity instanceof AstFunction function) {
        function.makeLambdaType();
      }
    }
    super.visit(node);
  }

  @Override
  protected void visit(AstFunctionHeader node) {
    super.visit(node);
  }

  @Override
  protected void visit(AstFunction node) {
    try {
      nameMap.beginScope();
      for (var parameter : node.header().parameters()) {
        registerLocal(parameter);
      }
      super.visit(node);
    } finally {
      nameMap.endScope();
    }
  }

  @Override
  protected void visit(AstVariable node) {
    super.visit(node);
    // Preprocessor has checked that we have typeRef or expression.
    if (node.type() == null) {
      node.type(node.expression().type());
    }
  }

  @Override
  protected void visit(AstTypeRef node) {
    super.visit(node);
  }

  @Override
  protected void visit(AstFunctionType node) {
    super.visit(node);
  }

  @Override
  protected void visit(AstRecordType node) {
    super.visit(node);
  }

  @Override
  protected void visit(AstUnionType node) {
    super.visit(node);
  }

  @Override
  protected void visit(AstBlock node) {
    super.visit(node);
  }

  @Override
  protected void visit(AstLocal node) {
    registerLocal(node.variable());
    super.visit(node);
  }

  private void registerLocal(AstVariable variable) {
    var existing = nameMap.addLocal(variable);
    if (existing != null) {
      errorConsumer.errorAt(
          variable.location(),
          "Duplicate local variable: '%s'. Defined at %s.",
          variable.name(),
          existing.location());
    }
  }

  protected void visit(AstDelete node) {
    super.visit(node);
    var expression = node.expression().value();
    switch (expression.type()) {
      case AstPointerType pointerType -> {
        // TODO: Generate defer call.
      }
      // case AstStringType stringType -> {
      //   // TODO: Free string
      // }
      // case AstArrayType arrayType -> {
      //   // TODO: Free array. Call defer.
      //
      // }
      default -> {
        errorConsumer.errorAt(
            node.location(), "Delete expression of type '%s', expected pointer", expression.type());
      }
    }
  }

  private static class ExpressionRewriter extends AstExpressionRewriter {
    private final NameMap nameMap;
    private final ErrorConsumer errorConsumer;

    ExpressionRewriter(NameMap nameMap, ErrorConsumer errorConsumer) {
      this.nameMap = nameMap;
      this.errorConsumer = errorConsumer;
    }

    @Override
    public AstExpression rewrite(AstEntityRef node) {
      if (node.isResolved()) {
        return node;
      }
      // Builtin names are resolved in AstCall, after the argument expressions are available.
      if (node.isBuiltin()) {
        return node;
      }
      var entity = nameMap.lookupEntity(node.name());
      if (entity == null) {
        errorConsumer.errorAt(node.location(), "Undefined name: '%s'", node.name());
        return node;
      }
      node.name(entity.name());
      node.entity(entity);
      return node;
    }

    @Override
    public AstExpression rewrite(AstFieldAccess node) {
      var objectType = checkNotNull(node.object().type());
      if (!(objectType instanceof AstRecordType recordType)) {
        errorConsumer.errorAt(
            node.location(), "Field access requires a record, got '%s'", objectType.name());
        return node;
      }
      for (var field : recordType.fields()) {
        if (field.name().entityName().equals(node.fieldName())) {
          node.field(field);
          return node;
        }
      }
      errorConsumer.errorAt(
          node.location(),
          "Undefined field '%s' in record '%s'",
          node.fieldName(),
          recordType.name());
      return node;
    }

    @Override
    public AstExpression rewrite(AstCall node) {
      // Callee and argument expressions have already been rewritten by AstCall.acceptRewriter.
      var function = node.function();
      if (function instanceof AstReceiverLookup receiverLookup) {
        resolveReceiverCall(node, receiverLookup);
      }
      function = node.function();
      // Builtin calls are selected here, once argument expressions are available.
      if (function instanceof AstEntityRef ref && ref.isBuiltin()) {
        checkState(!ref.isResolved());
        resolveBuiltinCall(node);
      }
      // Receiver lookup and builtin resolution may replace the callee expression.
      checkCallParameters(node);
      return node;
    }

    private void resolveBuiltinCall(AstCall node) {
      // TODO: Select the builtin overload using the resolved argument types.
      var operatorRef = (AstEntityRef) node.function();
      var function =
          switch (operatorRef.name().entityName()) {
            case "+" -> BuiltinFunctions.ADD_I64;
            case "*" -> BuiltinFunctions.MUL_I64;
            default ->
                throw new IllegalStateException(
                    "Unknown builtin entity '%s'".formatted(operatorRef.name()));
          };
      operatorRef.name(function.name());
      operatorRef.entity(function);
    }

    private void resolveReceiverCall(AstCall node, AstReceiverLookup receiverLookup) {
      var receiverType = receiverLookup.receiver().type();
      checkState(receiverType != null);
      if (receiverType == AstBuiltinType.VOID) {
        errorConsumer.errorAt(
            receiverLookup.location(),
            "Cannot resolve receiver function '%s' for null receiver",
            receiverLookup.functionName());
        return;
      }
      if (receiverType instanceof AstPointerType) {
        errorConsumer.errorAt(
            receiverLookup.location(), "Receiver of pointer is not allowed", receiverType.name());
        return;
      }
      var receiverFunctionName = receiverType.name().withEntity(receiverLookup.functionName());
      var function = nameMap.lookupReceiverFunction(receiverFunctionName);
      if (function == null) {
        errorConsumer.errorAt(
            receiverLookup.location(), "Undefined receiver function: '%s'", receiverFunctionName);
        return;
      }

      var functionRef = new AstEntityRef();
      functionRef.name(function.name());
      functionRef.entity(function);
      functionRef.location(receiverLookup.location());

      var arguments = new ArrayList<>(node.arguments());
      arguments.add(function.header().receiverIndex(), receiverLookup.receiver());
      node.arguments(arguments);
      node.function(functionRef);
    }

    private void checkCallParameters(AstCall call) {
      var type = checkNotNull(call.function().type());
      if (type instanceof AstFunctionType functionType) {
        var parameters = functionType.header().parameters();
        var arguments = call.arguments();
        if (arguments.size() != parameters.size()) {
          errorConsumer.errorAt(
              call.location(),
              "Function '%s' expects %d arguments, got %d",
              calleeExpressionMessage(call.function()),
              parameters.size(),
              arguments.size());
          return;
        }
        for (int i = 0; i < arguments.size(); i++) {
          var argumentType = checkNotNull(arguments.get(i).type());
          var parameterType = checkNotNull(parameters.get(i).type());
          if (!argumentType.equals(parameterType)) {
            errorConsumer.errorAt(
                arguments.get(i).location(),
                "Argument %d of function '%s' has type '%s', expected '%s'",
                i,
                calleeExpressionMessage(call.function()),
                argumentType.name(),
                parameterType.name());
          }
        }
      } else {
        errorConsumer.errorAt(
            call.function().location(),
            "Calling expression of type '%s', function expected.",
            type);
      }
    }

    private static String calleeExpressionMessage(AstExpression expression) {
      if (expression instanceof AstEntityRef entityRef) {
        return entityRef.name().toString();
      }
      return "function pointer";
    }
  }
}
