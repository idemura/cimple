package io.lang.cimple.compiler;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static io.lang.cimple.compiler.ast.AstBuiltinType.isIntegerType;
import com.google.common.collect.ImmutableList;
import io.lang.cimple.compiler.ast.AstArrayAccess;
import io.lang.cimple.compiler.ast.AstArrayType;
import io.lang.cimple.compiler.ast.AstBlock;
import io.lang.cimple.compiler.ast.AstBreak;
import io.lang.cimple.compiler.ast.AstBuiltinType;
import io.lang.cimple.compiler.ast.AstCall;
import io.lang.cimple.compiler.ast.AstCompoundAssign;
import io.lang.cimple.compiler.ast.AstDelete;
import io.lang.cimple.compiler.ast.AstEnumType;
import io.lang.cimple.compiler.ast.AstExpression;
import io.lang.cimple.compiler.ast.AstExpressionRewriteVisitor;
import io.lang.cimple.compiler.ast.AstFieldAccess;
import io.lang.cimple.compiler.ast.AstFor;
import io.lang.cimple.compiler.ast.AstFunction;
import io.lang.cimple.compiler.ast.AstFunctionHeader;
import io.lang.cimple.compiler.ast.AstFunctionPointerCall;
import io.lang.cimple.compiler.ast.AstFunctionRef;
import io.lang.cimple.compiler.ast.AstFunctionType;
import io.lang.cimple.compiler.ast.AstLocal;
import io.lang.cimple.compiler.ast.AstModule;
import io.lang.cimple.compiler.ast.AstNumberLiteral;
import io.lang.cimple.compiler.ast.AstPointerType;
import io.lang.cimple.compiler.ast.AstStringType;
import io.lang.cimple.compiler.ast.AstStructType;
import io.lang.cimple.compiler.ast.AstType;
import io.lang.cimple.compiler.ast.AstTypeHolder;
import io.lang.cimple.compiler.ast.AstTypeRef;
import io.lang.cimple.compiler.ast.AstUnionType;
import io.lang.cimple.compiler.ast.AstVariable;
import io.lang.cimple.compiler.ast.AstVariableRef;
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
    localNameMap = globalNameMap.collectVariables(module.name(), errorConsumer);
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
    node.name().copyValue(variable.name());
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
          node.location(), "Field access requires a struct, got '%s'", objectType.formatName());
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
    var function = node.function();
    if (!function.isResolved()) {
      if (function.isBuiltin()) {
        resolveBuiltinFunction(function);
      } else {
        resolveFunction(function, node.arguments());
      }
    }
    if (!function.isResolved()) {
      return node;
    }
    checkFunctionCallParameters(
        function.type(), node.arguments(), node.location(), calleeExpressionMessage(function));
    return node;
  }

  @Override
  public AstExpression rewrite(AstFunctionPointerCall node) {
    checkFunctionCallParameters(
        node.function().type(),
        node.arguments(),
        node.location(),
        calleeExpressionMessage(node.function()));
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
              throw new IllegalStateException(
                  "Unknown builtin entity '%s'".formatted(ref.name()));
        };
    ref.name().copyValue(function.name());
    ref.function(function);
  }

  private void resolveFunction(AstFunctionRef ref, List<AstExpression> arguments) {
    if (ref.isBuiltin()) {
      return;
    }
    var name = ref.name();
    var signature = callSignature(name.entity(), arguments);
    if (signature == null) {
      return;
    }
    var function = globalNameMap.lookupFunction(name.module(), signature);
    if (function == null) {
      errorConsumer.errorAt(
          ref.location(), "Undefined function: '%s'", formatSignature(name.module(), signature));
      return;
    }
    ref.name().copyValue(function.name());
    ref.function(function);
  }

  private static FunctionSignature callSignature(String name, List<AstExpression> arguments) {
    var argumentTypes = new ImmutableList.Builder<AstType>();
    for (var argument : arguments) {
      var type = argument.type();
      if (type == null) {
        return null;
      }
      argumentTypes.add(type);
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
      AstType type, List<AstExpression> arguments, Location location, String functionName) {
    if (type == null) {
      // A previous resolution error left the callee untyped; avoid a noisy follow-up error.
      return;
    }
    if (type instanceof AstFunctionType functionType) {
      checkFunctionArguments(functionType, arguments, location, functionName);
    } else {
      errorConsumer.errorAt(
          location, "Calling expression of type '%s', function expected.", type.formatName());
    }
  }

  private void checkBinaryOperatorArguments(
      AstFunctionRef operation, List<AstExpression> arguments, Location location) {
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
    if (expression instanceof AstVariableRef variableRef) {
      return variableRef.name().toString();
    }
    if (expression instanceof AstFunctionRef functionRef) {
      return functionRef.name().toString();
    }
    return "function pointer";
  }
}
