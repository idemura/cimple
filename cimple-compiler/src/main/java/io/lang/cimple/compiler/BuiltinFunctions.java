package io.lang.cimple.compiler;

import static io.lang.cimple.compiler.ast.AstBuiltinType.BOOL;
import static io.lang.cimple.compiler.ast.AstBuiltinType.INT64;

import com.google.common.collect.ImmutableList;
import io.lang.cimple.compiler.ast.AstBuiltinType;
import io.lang.cimple.compiler.ast.AstFunction;
import io.lang.cimple.compiler.ast.AstFunctionHeader;
import io.lang.cimple.compiler.ast.AstVariable;

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

  private BuiltinFunctions() {}

  static AstVariable makeParameter(String name, AstBuiltinType type) {
    var parameter = new AstVariable();
    parameter.name(Identifier.ofEntity(name));
    parameter.type(type);
    parameter.setBit(AstVariable.PARAMETER);
    return parameter;
  }

  static AstFunction makeBinaryOperator(
      String name, AstBuiltinType result, AstBuiltinType arg1, AstBuiltinType arg2) {
    return makeBuiltinFunction(
        name, result, ImmutableList.of(makeParameter("_0", arg1), makeParameter("_1", arg2)));
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
