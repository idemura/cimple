package com.github.idemura.cimple.compiler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Class that manages and resolves function overloads.
//
// The following overloads are supported:
//   * Overloads with same number of arguments if they all have the same prefix. Suffix
//     of parameters must be the same, but different for each overload, type:
//        f(a: string, x, y: i32);
//        f(a: string, x, y: f64);
//     Here "a" is a suffix, "x" and "y" is prefix of type i32 and f64.
//   * Every overload must return either the same type, or suffix type.
//        f(x, y: i32): i32
//        f(x, y: f64): f64
//     allowed, same as
//        f(x, y: i32): bool
//        f(x, y: f64): bool
//
final class FunctionOverload {
  static FunctionOverload create(List<AstFunction> defs) {
    var base = defs.getFirst();

    // Quick case
    if (defs.size() == 1) {
      return new FunctionOverload(defs, base.getParameters().size());
    }

    // Check all have the same number of arguments.
    int n = base.getParameters().size();
    for (var fn : defs) {
      if (fn.getParameters().size() != n) {
        // TODO: Add overloads to details.
        throw CompilerException.builder()
            .formatMessage("All overloads must have equal number of parameters")
            .setLocation(fn.getLocation())
            .build();
      }
    }

    // Check return types. They all are either the same, or equal to the suffix.
    var commonType = base.getResultType();
    int nCommon = 0;
    int nSuffix = 0;
    for (var fn : defs) {
      if (fn.getResultType().equals(commonType)) {
        nCommon++;
      }
      if (fn.getResultType().equals(fn.getOverloadType())) {
        nSuffix++;
      }
    }
    if (nCommon != defs.size()) {
      if (nSuffix != defs.size()) {
        throw CompilerException.builder()
            .formatMessage("Overload result type rule violated")
            .setLocation(base.getLocation())
            .build();
      }
    }

    // Get suffix length. This is the minimal suffix length over all overloads.
    int suffixLength = Integer.MAX_VALUE;
    for (var fn : defs) {
      suffixLength = Math.min(suffixLength, getSuffixLength(fn));
    }

    // Check prefix rule.
    for (var fn : defs) {
      if (!prefixEqual(base, fn, n - suffixLength)) {
        throw CompilerException.builder()
            .formatMessage("Overload prefix rule violated")
            .setLocation(fn.getLocation())
            .build();
      }
    }

    // Check all suffix are different types.
    Map<TypeRef, Boolean> unique = new HashMap<>();
    for (var fn : defs) {
      if (unique.put(fn.getOverloadType(), Boolean.TRUE) != null) {
        throw CompilerException.builder()
            .formatMessage("Overload suffix type duplication")
            .setLocation(fn.getLocation())
            .build();
      }
    }

    return new FunctionOverload(defs, n - suffixLength);
  }

  private final List<AstFunction> defs;
  private final int prefixLength;
  private final Map<TypeRef, AstFunction> overloadMap;

  private FunctionOverload(List<AstFunction> defs, int prefixLength) {
    this.defs = defs;
    this.prefixLength = prefixLength;
    this.overloadMap = defs.size() == 1 ? null : createOverloadMap(defs);
  }

  AstFunction resolve(List<TypeRef> args) {
    var base = defs.getFirst();

    if (args.size() != base.getParameters().size()) {
      throw CompilerException.builder()
          .formatMessage(
              "Apply %s: expected %s arguments, provided %s",
              base.getName(), base.getParameters().size(), args.size())
          .build();
    }

    int mismatchIndex = checkApplies(base, args, prefixLength);
    if (mismatchIndex >= 0) {
      var mismatchParam = base.getParameters().get(mismatchIndex);
      var parameterName =
          mismatchParam.getName().equals("_") ? "#" + mismatchIndex : mismatchParam.getName();
      throw CompilerException.builder()
          .formatMessage(
              "Apply %s: argument type does not apply to parameter %s",
              base.getName(), parameterName)
          .addDetail("Expected: %s", mismatchParam.getTypeRef())
          .addDetail("Provided: %s", args.get(mismatchIndex))
          .build();
    }

    if (defs.size() == 1) {
      return base;
    }

    // Check suffix arguments are all of the same (or compatible) type.
    // TODO: Promote types if needed - find "widest"
    for (int i = prefixLength; i < defs.size(); i++) {
      if (!args.get(i).equals(args.getLast())) {
        throw CompilerException.builder()
            .formatMessage("Overload %s: suffix of different types", base.getName())
            .build();
      }
    }

    // TODO: Promote types
    var def = overloadMap.get(args.getLast());
    if (def == null) {
      throw CompilerException.builder()
          .formatMessage("Overload %s: type not found", args.getLast())
          .build();
    }

    return def;
  }

  private static Map<TypeRef, AstFunction> createOverloadMap(List<AstFunction> defs) {
    Map<TypeRef, AstFunction> result = new HashMap<>();
    for (var f : defs) {
      result.put(f.getOverloadType(), f);
    }
    return result;
  }

  private int checkApplies(AstFunction def, List<TypeRef> args, int n) {
    var parameters = def.getParameters();
    for (int i = 0; i < n; i++) {
      // TODO: Type propagation
      if (!parameters.get(i).getTypeRef().equals(args.get(i))) {
        return i;
      }
    }
    return -1;
  }

  private static int getSuffixLength(AstFunction def) {
    var parameters = def.getParameters();
    final int n = parameters.size();
    if (n == 0) {
      return 0;
    }
    var suffixType = def.getOverloadType();
    int result = 0;
    for (int i = 1; i <= n; i++) {
      if (!suffixType.equals(parameters.get(n - i).getTypeRef())) {
        break;
      }
      result++;
    }
    return result;
  }

  private static boolean prefixEqual(AstFunction a, AstFunction b, int n) {
    var aParameters = a.getParameters();
    var bParameters = b.getParameters();
    for (int i = 0; i < n; i++) {
      var t1 = aParameters.get(i).getTypeRef();
      var t2 = bParameters.get(i).getTypeRef();
      if (!t1.equals(t2)) {
        return false;
      }
    }
    return true;
  }
}
