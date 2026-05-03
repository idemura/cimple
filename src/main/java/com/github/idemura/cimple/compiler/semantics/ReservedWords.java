package com.github.idemura.cimple.compiler.semantics;

import com.github.idemura.cimple.compiler.common.Keyword;
import com.google.common.collect.ImmutableSet;

public final class ReservedWords {
  private static final ImmutableSet<String> RESERVED_TYPE_NAMES =
      ImmutableSet.of(
          "bool", "byte", "char", "void", "int", "int32", "int64", "float32", "float64", "string");

  private ReservedWords() {}

  public static boolean isReservedWord(String name) {
    return Keyword.symbolNameMap().containsKey(name);
  }

  public static boolean isReservedTypeName(String name) {
    return isReservedWord(name) || RESERVED_TYPE_NAMES.contains(name);
  }
}
