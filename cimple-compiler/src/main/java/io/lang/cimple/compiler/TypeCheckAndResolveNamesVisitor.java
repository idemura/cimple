package io.lang.cimple.compiler;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static io.lang.cimple.compiler.AstBuiltinType.isIntegerType;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableList;
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
    localNameMap = globalNameMap.collectVariables(module.name().entity(), errorConsumer);
    super.visit(node);
  }

  @Override
  protected void visit(AstFunction node) {
    try {
      localNameMap.beginScope();
      for (var parameter : node.parameters()) {
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
      var valueExpression = variant.expression();
      if (valueExpression != null) {
        if (valueExpression instanceof AstNumberLiteral) {
          var valueType = checkNotNull(valueExpression.type());
          if (!isIntegerType(valueType)) {
            errorConsumer.errorAt(
                variant.tag().location(),
                "Enum variant '%s' value has type '%s', expected integer",
                variant.tag(),
                valueType);
          }
        } else {
          // TODO: Remove one constant folding works.
          errorConsumer.errorAt(
              variant.tag().location(),
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
    var expression = node.expression();
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
            node.location(), "Delete expression of type '%s', expected pointer", expression.type());
      }
    }
  }

  @Override
  public AstExpression rewrite(AstVariableRef node) {
    checkArgument(!node.isBuiltin());
    if (node.isResolved()) {
      return node;
    }
    var variable = lookupVariable(node.name());
    if (variable == null) {
      errorConsumer.errorAt(node.location(), "Undefined name: '%s'", node.name());
      return node;
    }
    node.name().assign(variable.name());
    node.variable(variable);
    return node;
  }

  @Override
  public AstExpression rewrite(AstFunctionRef node) {
    if (!node.isResolved() && node.isBuiltin()) {
      resolveBuiltinFunction(node);
    }
    return node;
  }

  @Override
  public AstExpression rewrite(AstFieldAccess node) {
    var objectType = checkNotNull(node.object().type());
    if (!(objectType instanceof AstStructType structType)) {
      errorConsumer.errorAt(
          node.location(), "Field access requires a struct, got '%s'", objectType);
      return node;
    }
    for (var field : structType.fields()) {
      if (field.name().entity().equals(node.fieldName())) {
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
      errorConsumer.errorAt(node.location(), "Array access requires an array, got '%s'", arrayType);
    }
    var indexType = checkNotNull(node.index().type());
    if (!AstBuiltinType.INT64.equals(indexType)) {
      errorConsumer.errorAt(
          node.index().location(), "Array index has type '%s', expected 'int64'", indexType);
    }
    return node;
  }

  @Override
  public AstExpression rewrite(AstCall node) {
    var function = node.function();
    if (!function.isResolved()) {
      if (function.isBuiltin()) {
        resolveBuiltinFunction(function);
      } else {
        node.deducedWildcards(resolveFunction(function, node.arguments()));
      }
    }
    if (!function.isResolved()) {
      return node;
    }
    checkFunctionArguments(
        function.function(),
        node.arguments(),
        node.location(),
        calleeExpressionMessage(function),
        node.deducedWildcards());
    return node;
  }

  @Override
  public AstExpression rewrite(AstFunctionPointerCall node) {
    var function = node.function();
    if (function.type() instanceof AstFunctionType functionType) {
      checkFunctionArguments(
          functionType.function(),
          node.arguments(),
          node.location(),
          calleeExpressionMessage(node.function()),
          ImmutableMap.of());
    } else {
      errorConsumer.errorAt(
          node.location(), "Calling expression of type '%s', function expected.", function.type());
    }
    return node;
  }

  @Override
  public AstExpression rewrite(AstCompoundAssign node) {
    resolveBuiltinFunction(node.operation());
    checkBinaryOperatorArguments(
        node.operation(), List.of(node.target(), node.value()), node.location());
    return node;
  }

  private void resolveBuiltinFunction(AstFunctionRef ref) {
    // TODO: Select the builtin overload using the resolved argument types.
    var function =
        switch (ref.name().entity()) {
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
              throw new IllegalStateException("Unknown builtin entity '%s'".formatted(ref.name()));
        };
    ref.name().assign(function.name());
    ref.function(function);
  }

  private ImmutableMap<AstTypeWildcard, AstType> resolveFunction(
      AstFunctionRef ref, List<AstExpression> arguments) {
    if (ref.isBuiltin()) {
      return ImmutableMap.of();
    }
    var name = ref.name();
    var signature = callSignature(name.entity(), arguments);
    if (signature == null) {
      return ImmutableMap.of();
    }
    var match = globalNameMap.lookupFunctionMatch(name.module(), signature);
    if (match == null) {
      errorConsumer.errorAt(
          ref.location(), "Undefined function: '%s'", formatSignature(name.module(), signature));
      return ImmutableMap.of();
    }
    var function = match.function();
    ref.name().assign(function.name());
    ref.function(function);
    return match.substitutions();
  }

  private static FunctionSignature callSignature(String name, List<AstExpression> arguments) {
    var argumentTypes = new ImmutableList.Builder<AstType>();
    for (var argument : arguments) {
      argumentTypes.add(checkNotNull(argument.type()));
    }
    return new FunctionSignature(name, argumentTypes.build());
  }

  private static String formatSignature(String moduleName, FunctionSignature signature) {
    if (moduleName == null) {
      return signature.toString();
    }
    return "%s~%s".formatted(moduleName, signature);
  }

  private AstVariable lookupVariable(Identifier name) {
    if (name.module() == null) {
      return localNameMap.lookupVariable(name.entity());
    }
    return globalNameMap.lookupVariable(name);
  }

  private void checkFunctionCallParameters(
      AstFunction function, List<AstExpression> arguments, Location location, String functionName) {
    // if (function == null) {
    //   // A previous resolution error left the callee untyped; avoid a noisy follow-up error.
    //   return;
    // }
  }

  private void checkBinaryOperatorArguments(
      AstFunctionRef operation, List<AstExpression> arguments, Location location) {
    checkFunctionArguments(operation.function(), arguments, location, operation.name().toString());
  }

  private void checkFunctionArguments(
      AstFunction function, List<AstExpression> arguments, Location location, String functionName) {
    checkFunctionArguments(function, arguments, location, functionName, ImmutableMap.of());
  }

  private void checkFunctionArguments(
      AstFunction function,
      List<AstExpression> arguments,
      Location location,
      String functionName,
      ImmutableMap<AstTypeWildcard, AstType> genericArguments) {
    var parameters = function.parameters();
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
      var parameterType = checkNotNull(parameters.get(i).type()).substitute(genericArguments);
      if (!argumentType.equals(parameterType)) {
        errorConsumer.errorAt(
            arguments.get(i).location(),
            "Argument %d of function '%s' has type '%s', expected '%s'",
            i,
            functionName,
            argumentType,
            parameterType);
      }
    }
  }

  private static String calleeExpressionMessage(AstExpression expression) {
    if (expression instanceof AstVariableRef variableRef) {
      return variableRef.name().toString();
    }
    if (expression instanceof AstFunctionRef functionRef) {
      return functionRef.name().toString();
    }
    return "function pointer";
  }
}
