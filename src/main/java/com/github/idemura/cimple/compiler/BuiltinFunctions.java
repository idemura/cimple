package com.github.idemura.cimple.compiler;

import java.util.List;

final class BuiltinFunctions {
  private static final List<AstFunction> ALL =
      List.of(
          AstFunction.std("+", BuiltinTypes.INT32, List.of(BuiltinTypes.INT32, BuiltinTypes.INT32)),
          AstFunction.std("-", BuiltinTypes.INT32, List.of(BuiltinTypes.INT32, BuiltinTypes.INT32)),
          AstFunction.std("*", BuiltinTypes.INT32, List.of(BuiltinTypes.INT32, BuiltinTypes.INT32)),
          AstFunction.std("/", BuiltinTypes.INT32, List.of(BuiltinTypes.INT32, BuiltinTypes.INT32)),
          AstFunction.std("<", BuiltinTypes.BOOL, List.of(BuiltinTypes.INT32, BuiltinTypes.INT32)),
          AstFunction.std(">", BuiltinTypes.BOOL, List.of(BuiltinTypes.INT32, BuiltinTypes.INT32)));

  static List<AstFunction> getList() {
    return ALL;
  }
}
