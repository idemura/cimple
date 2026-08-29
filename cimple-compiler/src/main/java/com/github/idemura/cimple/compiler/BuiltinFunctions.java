package com.github.idemura.cimple.compiler;

import static com.github.idemura.cimple.compiler.ast.AstBuiltinType.BOOL;
import static com.github.idemura.cimple.compiler.ast.AstBuiltinType.INT64;

import com.github.idemura.cimple.compiler.ast.AstBuiltinType;
import com.github.idemura.cimple.compiler.ast.AstEntity;
import com.github.idemura.cimple.compiler.ast.AstFunction;
import com.github.idemura.cimple.compiler.ast.AstFunctionHeader;
import com.github.idemura.cimple.compiler.ast.AstVariable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public final class BuiltinFunctions {
  private static final String APPEND = "append";
  private static final String CAPACITY = "capacity";
  private static final String SIZE = "size";

  public static final AstFunction ADD_I64 = makeBinaryOperator("_add_i64", INT64, INT64, INT64);
  public static final AstFunction SUB_I64 = makeBinaryOperator("_sub_i64", INT64, INT64, INT64);
  public static final AstFunction MUL_I64 = makeBinaryOperator("_mul_i64", INT64, INT64, INT64);
  public static final AstFunction DIV_I64 = makeBinaryOperator("_div_i64", INT64, INT64, INT64);
  public static final AstFunction MOD_I64 = makeBinaryOperator("_mod_i64", INT64, INT64, INT64);
  public static final AstFunction EQ_I64 = makeBinaryOperator("_eq_i64", BOOL, INT64, INT64);
  public static final AstFunction NE_I64 = makeBinaryOperator("_ne_i64", BOOL, INT64, INT64);
  public static final AstFunction LT_I64 = makeBinaryOperator("_lt_i64", BOOL, INT64, INT64);
  public static final AstFunction LE_I64 = makeBinaryOperator("_le_i64", BOOL, INT64, INT64);
  public static final AstFunction GT_I64 = makeBinaryOperator("_gt_i64", BOOL, INT64, INT64);
  public static final AstFunction GE_I64 = makeBinaryOperator("_ge_i64", BOOL, INT64, INT64);
  public static final AstFunction ARRAY_APPEND = makeArrayMethod(APPEND, AstBuiltinType.VOID, 1);
  public static final AstFunction ARRAY_CAPACITY = makeArrayMethod(CAPACITY, INT64, 0);
  public static final AstFunction ARRAY_SIZE = makeArrayMethod(SIZE, INT64, 0);

  private static final ImmutableMap<String, AstFunction> ARRAY_METHODS =
      ImmutableMap.<String, AstFunction>builder()
          .put(APPEND, ARRAY_APPEND)
          .put(CAPACITY, ARRAY_CAPACITY)
          .put(SIZE, ARRAY_SIZE)
          .build();

  private BuiltinFunctions() {}

  static AstFunction lookupArrayMethod(String name) {
    return ARRAY_METHODS.get(name);
  }

  static boolean isArrayMethod(AstEntity entity) {
    return entity == ARRAY_APPEND || entity == ARRAY_CAPACITY || entity == ARRAY_SIZE;
  }

  static AstVariable makeParameter(String name, AstBuiltinType type) {
    var parameter = new AstVariable();
    parameter.name(Identifier.ofEntity(name));
    parameter.type(type);
    parameter.setBit(AstVariable.PARAMETER);
    return parameter;
  }

  private static AstVariable makePolymorphicParameter(String name) {
    var parameter = new AstVariable();
    parameter.name(Identifier.ofEntity(name));
    parameter.setBit(AstVariable.PARAMETER);
    return parameter;
  }

  static AstFunction makeBinaryOperator(
      String name, AstBuiltinType result, AstBuiltinType arg1, AstBuiltinType arg2) {
    return makeBuiltinFunction(
        name, result, ImmutableList.of(makeParameter("_0", arg1), makeParameter("_1", arg2)));
  }

  private static AstFunction makeArrayMethod(
      String name, AstBuiltinType resultType, int extraParameterCount) {
    // The synthetic object parameter is type-checked by NameResolutionVisitor because the AST
    // does not yet have a way to express "array of any element type" as a concrete parameter type.
    var parameters = new ImmutableList.Builder<AstVariable>();
    parameters.add(makePolymorphicParameter("_0"));
    for (var i = 0; i < extraParameterCount; i++) {
      parameters.add(makePolymorphicParameter("_" + (i + 1)));
    }
    return makeBuiltinFunction(name, resultType, parameters.build());
  }

  private static AstFunction makeBuiltinFunction(
      String name, AstBuiltinType resultType, ImmutableList<AstVariable> parameters) {
    var header = new AstFunctionHeader();
    header.parameters(parameters);
    header.resultType(resultType);
    var function = new AstFunction();
    function.name(Identifier.ofEntity(name).builtin());
    function.header(header);
    function.makeLambdaType();
    return function;
  }
}
