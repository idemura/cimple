package com.github.idemura.cimple.compiler;

import static com.github.idemura.cimple.compiler.ast.AstBuiltinType.isIntegerType;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.github.idemura.cimple.compiler.ast.AstArrayAccess;
import com.github.idemura.cimple.compiler.ast.AstArrayType;
import com.github.idemura.cimple.compiler.ast.AstBlock;
import com.github.idemura.cimple.compiler.ast.AstBreak;
import com.github.idemura.cimple.compiler.ast.AstBuiltinType;
import com.github.idemura.cimple.compiler.ast.AstCall;
import com.github.idemura.cimple.compiler.ast.AstCompoundAssign;
import com.github.idemura.cimple.compiler.ast.AstDelete;
import com.github.idemura.cimple.compiler.ast.AstEntity;
import com.github.idemura.cimple.compiler.ast.AstEntityRef;
import com.github.idemura.cimple.compiler.ast.AstEnumType;
import com.github.idemura.cimple.compiler.ast.AstExpression;
import com.github.idemura.cimple.compiler.ast.AstExpressionRewriteVisitor;
import com.github.idemura.cimple.compiler.ast.AstFieldAccess;
import com.github.idemura.cimple.compiler.ast.AstFor;
import com.github.idemura.cimple.compiler.ast.AstFunction;
import com.github.idemura.cimple.compiler.ast.AstFunctionHeader;
import com.github.idemura.cimple.compiler.ast.AstFunctionType;
import com.github.idemura.cimple.compiler.ast.AstLocal;
import com.github.idemura.cimple.compiler.ast.AstModule;
import com.github.idemura.cimple.compiler.ast.AstNumberLiteral;
import com.github.idemura.cimple.compiler.ast.AstPointerType;
import com.github.idemura.cimple.compiler.ast.AstStringType;
import com.github.idemura.cimple.compiler.ast.AstStructType;
import com.github.idemura.cimple.compiler.ast.AstType;
import com.github.idemura.cimple.compiler.ast.AstTypeHolder;
import com.github.idemura.cimple.compiler.ast.AstTypeRef;
import com.github.idemura.cimple.compiler.ast.AstUnionType;
import com.github.idemura.cimple.compiler.ast.AstVariable;
import java.util.ArrayList;
import java.util.List;

public class TypeCheckAndResolveNamesVisitor extends AstExpressionRewriteVisitor {
  private final GlobalNameMap globalNameMap;
  private final ErrorConsumer errorConsumer;
  private LocalNameMap localNameMap;
  private AstModule module;
  private int loopDepth;

  public TypeCheckAndResolveNamesVisitor(GlobalNameMap globalNameMap, ErrorConsumer errorConsumer) {
    this.globalNameMap = globalNameMap;
    this.errorConsumer = errorConsumer;
  }

  @Override
  protected void visit(AstModule node) {
    module = node;
    localNameMap = globalNameMap.collectFunctionsAndVariables(module, errorConsumer);
    super.visit(node);
  }

  @Override
  protected void visit(AstFunctionHeader node) {
    super.visit(node);
  }

  @Override
  protected void visit(AstFunction node) {
    try {
      localNameMap.beginScope();
      for (var parameter : node.header().parameters()) {
        registerLocal(parameter);
      }
      super.visit(node);
    } finally {
      localNameMap.endScope();
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
  protected void visit(AstTypeHolder node) {}

  @Override
  protected void visit(AstTypeRef node) {
    super.visit(node);
  }

  @Override
  protected void visit(AstFunctionType node) {
    super.visit(node);
  }

  @Override
  protected void visit(AstStructType node) {
    super.visit(node);
  }

  @Override
  protected void visit(AstUnionType node) {
    super.visit(node);
  }

  @Override
  protected void visit(AstEnumType node) {
    super.visit(node);

    for (var variant : node.variants()) {
      var valueExpression = variant.valueExpression();
      if (valueExpression != null) {
        if (valueExpression instanceof AstNumberLiteral) {
          var valueType = checkNotNull(valueExpression.type());
          if (!isIntegerType(valueType)) {
            errorConsumer.errorAt(
                variant.location(),
                "Enum variant '%s' value has type '%s', expected integer",
                variant.tag(),
                valueType.formatName());
          }
        } else {
          // TODO: Remove one constant folding works.
          errorConsumer.errorAt(
              variant.location(),
              "Enum variant '%s' value must be a number literal",
              variant.tag());
        }
      }
    }
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

  @Override
  protected void visit(AstFor node) {
    try {
      localNameMap.beginScope();
      loopDepth++;
      super.visit(node);
    } finally {
      loopDepth--;
      localNameMap.endScope();
    }
  }

  @Override
  protected void visit(AstBreak node) {
    if (loopDepth == 0) {
      errorConsumer.errorAt(node.location(), "'break' is only allowed inside a loop");
    }
    super.visit(node);
  }

  private void registerLocal(AstVariable variable) {
    var existing = localNameMap.addLocal(variable);
    if (existing != null) {
      errorConsumer.errorAt(
          variable.location(),
          "Duplicate local variable: '%s'. Defined at %s.",
          variable.name(),
          existing.location());
    }
  }

  @Override
  protected void visit(AstDelete node) {
    super.visit(node);
    var expression = node.expression().get();
    switch (expression.type()) {
      case AstPointerType pointerType -> {
        // TODO: Generate defer call.
      }
      case AstStringType stringType -> {
        // TODO: Delete string.
      }
      case AstArrayType arrayType -> {
        // TODO: Delete array. Call defer on elements.

      }
      default -> {
        errorConsumer.errorAt(
            node.location(),
            "Delete expression of type '%s', expected pointer",
            expression.type().formatName());
      }
    }
  }

  @Override
  public AstExpression rewrite(AstEntityRef node) {
    if (node.isResolved()) {
      return node;
    }
    // Parser-created operator references are already tagged as builtins.
    if (node.isBuiltin()) {
      return node;
    }
    var entity = lookupEntity(node.name());
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
    if (node.method()) {
      return node;
    }
    var objectType = checkNotNull(node.object().type());
    if (!(objectType instanceof AstStructType structType)) {
      errorConsumer.errorAt(
          node.location(), "Field access requires a struct, got '%s'", objectType.formatName());
      return node;
    }
    for (var field : structType.fields()) {
      if (field.name().entityName().equals(node.fieldName())) {
        node.field(field);
        return node;
      }
    }
    errorConsumer.errorAt(
        node.location(),
        "Undefined field '%s' in struct '%s'",
        node.fieldName(),
        structType.name());
    return node;
  }

  @Override
  public AstExpression rewrite(AstArrayAccess node) {
    var arrayType = checkNotNull(node.array().type());
    if (!(arrayType instanceof AstArrayType)) {
      errorConsumer.errorAt(
          node.location(), "Array access requires an array, got '%s'", arrayType.formatName());
    }
    var indexType = checkNotNull(node.index().type());
    if (!AstBuiltinType.INT64.equals(indexType)) {
      errorConsumer.errorAt(
          node.index().location(),
          "Array index has type '%s', expected 'int64'",
          indexType.formatName());
    }
    return node;
  }

  @Override
  public AstExpression rewrite(AstCall node) {
    // Callee and argument expressions have already been rewritten by AstCall.acceptRewriter.
    var function = node.function();
    if (function instanceof AstFieldAccess field && field.method()) {
      resolveMethodCall(node, field);
    }
    function = node.function();
    // Builtin calls are selected here, once argument expressions are available.
    if (function instanceof AstEntityRef ref && ref.isBuiltin()) {
      if (BuiltinFunctions.isArrayMethod(ref.entity())) {
        return node;
      }
      if (!ref.isResolved()) {
        resolveBuiltinFunction(ref);
      }
    }
    // Method lookup and builtin resolution may replace the callee expression.
    checkCallParameters(node);
    return node;
  }

  @Override
  public AstExpression rewrite(AstCompoundAssign node) {
    resolveBuiltinFunction(node.operation());
    checkBinaryOperatorArguments(
        node.operation(), List.of(node.target(), node.value()), node.location());
    return node;
  }

  private void resolveBuiltinFunction(AstEntityRef operatorRef) {
    // TODO: Select the builtin overload using the resolved argument types.
    var function =
        switch (operatorRef.name().entityName()) {
          case "+" -> BuiltinFunctions.ADD_I64;
          case "-" -> BuiltinFunctions.SUB_I64;
          case "*" -> BuiltinFunctions.MUL_I64;
          case "/" -> BuiltinFunctions.DIV_I64;
          case "%" -> BuiltinFunctions.MOD_I64;
          case "==" -> BuiltinFunctions.EQ_I64;
          case "!=" -> BuiltinFunctions.NE_I64;
          case "<" -> BuiltinFunctions.LT_I64;
          case "<=" -> BuiltinFunctions.LE_I64;
          case ">" -> BuiltinFunctions.GT_I64;
          case ">=" -> BuiltinFunctions.GE_I64;
          default ->
              throw new IllegalStateException(
                  "Unknown builtin entity '%s'".formatted(operatorRef.name()));
        };
    operatorRef.name(function.name());
    operatorRef.entity(function);
  }

  private void resolveMethodCall(AstCall node, AstFieldAccess fieldAccess) {
    var objectType = fieldAccess.object().type();
    checkState(objectType != null);
    if (objectType instanceof AstFunctionType && fieldAccess.fieldName().equals("call")) {
      // Function values reserve `.call(...)` as explicit invocation syntax.
      node.function(fieldAccess.object());
      return;
    }
    if (objectType instanceof AstArrayType arrayType) {
      resolveArrayMethodCall(node, fieldAccess, arrayType);
      return;
    }
    if (objectType == AstBuiltinType.VOID) {
      errorConsumer.errorAt(
          fieldAccess.location(),
          "Cannot resolve method '%s' for null object",
          fieldAccess.fieldName());
      return;
    }
    if (objectType instanceof AstPointerType) {
      errorConsumer.errorAt(
          fieldAccess.location(), "Method of pointer is not allowed", objectType.name());
      return;
    }
    var methodName = objectType.name().withEntity(fieldAccess.fieldName());
    var function = lookupMethod(objectType, fieldAccess.fieldName());
    if (function == null) {
      errorConsumer.errorAt(fieldAccess.location(), "Undefined method: '%s'", methodName);
      return;
    }

    var functionRef = new AstEntityRef();
    functionRef.name(function.name());
    functionRef.entity(function);
    functionRef.location(fieldAccess.location());

    var arguments = new ArrayList<>(node.arguments());
    arguments.add(function.header().objectIndex(), fieldAccess.object());
    node.arguments(arguments);
    node.function(functionRef);
  }

  private AstFunction lookupMethod(AstType objectType, String fieldName) {
    return lookupFunction(objectType.name().withEntity(fieldName));
  }

  private AstFunction lookupFunction(Identifier name) {
    return globalNameMap.lookupEntity(name) instanceof AstFunction function ? function : null;
  }

  private AstEntity lookupEntity(Identifier name) {
    if (name.moduleName() == null) {
      return localNameMap.lookupEntity(name);
    }
    return globalNameMap.lookupEntity(name);
  }

  private void resolveArrayMethodCall(
      AstCall node, AstFieldAccess fieldAccess, AstArrayType arrayType) {
    var function = BuiltinFunctions.lookupArrayMethod(fieldAccess.fieldName());
    if (function == null) {
      errorConsumer.errorAt(
          fieldAccess.location(), "Undefined array method: '%s'", fieldAccess.fieldName());
      return;
    }
    if (function == BuiltinFunctions.ARRAY_APPEND) {
      checkArrayAppendArguments(node, fieldAccess, arrayType);
    } else {
      checkArrayMethodArgumentCount(node, fieldAccess, 0);
    }

    var functionRef = new AstEntityRef();
    functionRef.name(function.name());
    functionRef.entity(function);
    functionRef.location(fieldAccess.location());

    var arguments = new ArrayList<>(node.arguments());
    arguments.add(0, fieldAccess.object());
    node.arguments(arguments);
    node.function(functionRef);
  }

  private void checkArrayAppendArguments(
      AstCall node, AstFieldAccess fieldAccess, AstArrayType arrayType) {
    if (!checkArrayMethodArgumentCount(node, fieldAccess, 1)) {
      return;
    }
    var value = node.arguments().get(0);
    var valueType = checkNotNull(value.type());
    var elementType = arrayType.baseType();
    if (!valueType.equals(elementType)) {
      errorConsumer.errorAt(
          value.location(),
          "Array method 'append' argument has type '%s', expected '%s'",
          valueType.formatName(),
          elementType.formatName());
    }
  }

  private boolean checkArrayMethodArgumentCount(
      AstCall node, AstFieldAccess fieldAccess, int expectedCount) {
    if (node.arguments().size() == expectedCount) {
      return true;
    }
    errorConsumer.errorAt(
        node.location(),
        "Array method '%s' expects %d arguments, got %d",
        fieldAccess.fieldName(),
        expectedCount,
        node.arguments().size());
    return false;
  }

  private void checkCallParameters(AstCall call) {
    var type = checkNotNull(call.function().type());
    if (type instanceof AstFunctionType functionType) {
      checkFunctionArguments(
          functionType,
          call.arguments(),
          call.location(),
          calleeExpressionMessage(call.function()));
    } else {
      errorConsumer.errorAt(
          call.function().location(),
          "Calling expression of type '%s', function expected.",
          type.formatName());
    }
  }

  private void checkBinaryOperatorArguments(
      AstEntityRef operation, List<AstExpression> arguments, Location location) {
    var type = checkNotNull(operation.type());
    if (type instanceof AstFunctionType functionType) {
      checkFunctionArguments(functionType, arguments, location, operation.name().toString());
    } else {
      errorConsumer.errorAt(
          operation.location(),
          "Operator expression of type '%s', function expected.",
          type.formatName());
    }
  }

  private void checkFunctionArguments(
      AstFunctionType functionType,
      List<AstExpression> arguments,
      Location location,
      String functionName) {
    var parameters = functionType.header().parameters();
    if (arguments.size() != parameters.size()) {
      errorConsumer.errorAt(
          location,
          "Function '%s' expects %d arguments, got %d",
          functionName,
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
            functionName,
            argumentType.formatName(),
            parameterType.formatName());
      }
    }
  }

  private static String calleeExpressionMessage(AstExpression expression) {
    if (expression instanceof AstEntityRef entityRef) {
      return entityRef.name().toString();
    }
    return "function pointer";
  }
}
