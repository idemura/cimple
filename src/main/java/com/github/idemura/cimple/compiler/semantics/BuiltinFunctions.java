package com.github.idemura.cimple.compiler.semantics;

import static com.github.idemura.cimple.compiler.ast.AstBuiltinType.INT64;

import com.github.idemura.cimple.compiler.QualifiedName;
import com.github.idemura.cimple.compiler.ast.AstBuiltinType;
import com.github.idemura.cimple.compiler.ast.AstFunction;
import com.github.idemura.cimple.compiler.ast.AstFunctionHeader;
import com.github.idemura.cimple.compiler.ast.AstTypeRef;
import com.github.idemura.cimple.compiler.ast.AstVariable;
import com.google.common.collect.ImmutableList;

public final class BuiltinFunctions {
  public static final AstFunction ADD_I64 = makeBinaryOperator("_add_i64", INT64, INT64, INT64);
  public static final AstFunction MUL_I64 = makeBinaryOperator("_mul_i64", INT64, INT64, INT64);

  private BuiltinFunctions() {}

  static AstVariable makeParameter(String name, AstBuiltinType type) {
    var parameter = new AstVariable();
    parameter.name(QualifiedName.ofEntity(name));
    parameter.typeRef(AstTypeRef.ofType(type));
    parameter.setBit(AstVariable.PARAMETER);
    return parameter;
  }

  static AstFunction makeBinaryOperator(
      String name, AstBuiltinType result, AstBuiltinType arg1, AstBuiltinType arg2) {
    var header = new AstFunctionHeader();
    header.parameters(ImmutableList.of(makeParameter("_0", arg1), makeParameter("_1", arg2)));
    header.resultType(AstTypeRef.ofType(result));
    var function = new AstFunction();
    function.name(QualifiedName.ofEntity(name).builtin());
    function.header(header);
    function.markResolved();
    return function;
  }
}
