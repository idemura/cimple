package com.github.idemura.cimple.compiler.semantics;

import static com.github.idemura.cimple.compiler.ast.AstBuiltinType.BOOL;
import static com.github.idemura.cimple.compiler.ast.AstBuiltinType.INT64;

import com.github.idemura.cimple.compiler.Identifier;
import com.github.idemura.cimple.compiler.ast.AstBuiltinType;
import com.github.idemura.cimple.compiler.ast.AstFunction;
import com.github.idemura.cimple.compiler.ast.AstFunctionHeader;
import com.github.idemura.cimple.compiler.ast.AstVariable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public final class BuiltinFunctions {
  private static final String SIZE = "size";

  public static final AstFunction ADD_I64 = makeBinaryOperator("_add_i64", INT64, INT64, INT64);
  public static final AstFunction SUB_I64 = makeBinaryOperator("_sub_i64", INT64, INT64, INT64);
  public static final AstFunction MUL_I64 = makeBinaryOperator("_mul_i64", INT64, INT64, INT64);
  public static final AstFunction DIV_I64 = makeBinaryOperator("_div_i64", INT64, INT64, INT64);
  public static final AstFunction MOD_I64 = makeBinaryOperator("_mod_i64", INT64, INT64, INT64);
  public static final AstFunction LT_I64 = makeBinaryOperator("_lt_i64", BOOL, INT64, INT64);
  public static final AstFunction GT_I64 = makeBinaryOperator("_gt_i64", BOOL, INT64, INT64);
  public static final AstFunction ARRAY_SIZE = makeArrayMethod(SIZE, INT64);

  private static final ImmutableMap<String, AstFunction> ARRAY_METHODS =
      ImmutableMap.of(SIZE, ARRAY_SIZE);

  private BuiltinFunctions() {}

  static AstFunction lookupArrayMethod(String name) {
    return ARRAY_METHODS.get(name);
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

  private static AstFunction makeArrayMethod(String name, AstBuiltinType resultType) {
    // The synthetic object parameter is type-checked by NameResolutionVisitor because the AST
    // does not yet have a way to express "array of any element type" as a concrete parameter type.
    return makeBuiltinFunction(name, resultType, ImmutableList.of(makePolymorphicParameter("_0")));
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
