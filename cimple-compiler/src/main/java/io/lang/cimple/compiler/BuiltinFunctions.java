package io.lang.cimple.compiler;

import static io.lang.cimple.compiler.AstBuiltinType.BOOL;
import static io.lang.cimple.compiler.AstBuiltinType.INT64;

import com.google.common.collect.ImmutableList;

public final class BuiltinFunctions {
  private static final String APPEND = "append";
  private static final String CAPACITY = "capacity";
  private static final String SIZE = "size";

  public static final AstFunction ADD_I64 = makeOperator("_add_i64", INT64, INT64, INT64);
  public static final AstFunction SUB_I64 = makeOperator("_sub_i64", INT64, INT64, INT64);
  public static final AstFunction MUL_I64 = makeOperator("_mul_i64", INT64, INT64, INT64);
  public static final AstFunction DIV_I64 = makeOperator("_div_i64", INT64, INT64, INT64);
  public static final AstFunction MOD_I64 = makeOperator("_mod_i64", INT64, INT64, INT64);
  public static final AstFunction EQ_I64 = makeOperator("_eq_i64", BOOL, INT64, INT64);
  public static final AstFunction NE_I64 = makeOperator("_ne_i64", BOOL, INT64, INT64);
  public static final AstFunction LT_I64 = makeOperator("_lt_i64", BOOL, INT64, INT64);
  public static final AstFunction LE_I64 = makeOperator("_le_i64", BOOL, INT64, INT64);
  public static final AstFunction GT_I64 = makeOperator("_gt_i64", BOOL, INT64, INT64);
  public static final AstFunction GE_I64 = makeOperator("_ge_i64", BOOL, INT64, INT64);

  private BuiltinFunctions() {}

  static AstVariable makeParameter(String name, AstType type) {
    var parameter = new AstVariable(new Identifier(name), type, null);
    parameter.flags(AstVariable.PARAMETER);
    return parameter;
  }

  private static AstFunction makeOperator(
      String name, AstType resultType, AstType arg1, AstType arg2) {
    return makeBuiltinFunction(
        name, resultType, ImmutableList.of(makeParameter("_0", arg1), makeParameter("_1", arg2)));
  }

  private static AstFunction makeBuiltinFunction(
      String name, AstType resultType, ImmutableList<AstVariable> parameters) {
    return new AstFunction(
        new Identifier(name).builtin(), ImmutableList.of(), parameters, resultType, null);
  }
}
