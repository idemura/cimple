package com.github.idemura.cimple.compiler.semantics;

import com.github.idemura.cimple.compiler.common.Keyword;
import com.google.common.collect.ImmutableSet;

public final class ReservedWords {
  private static final ImmutableSet<String> TYPE_DECLARATION_NAMES =
      ImmutableSet.of("class", "interface", "opaque", "record", "template", "union");

  private static final ImmutableSet<String> RESERVED_WORDS = createReservedWords();

  private static final ImmutableSet<String> RESERVED_TYPE_NAMES =
      new ImmutableSet.Builder<String>()
          .addAll(RESERVED_WORDS)
          .addAll(TYPE_DECLARATION_NAMES)
          .add(
              "bool", "byte", "char", "float", "float32", "float64", "int", "int32", "int64",
              "string", "void")
          .build();

  private ReservedWords() {}

  private static ImmutableSet<String> createReservedWords() {
    var builder = new ImmutableSet.Builder<String>();
    for (var name : Keyword.symbolNameMap().keySet()) {
      if (!TYPE_DECLARATION_NAMES.contains(name)) {
        builder.add(name);
      }
    }
    builder.add("true", "false", "null");
    return builder.build();
  }

  public static boolean isReservedWord(String name) {
    return RESERVED_WORDS.contains(name);
  }

  public static boolean isReservedTypeName(String name) {
    return RESERVED_TYPE_NAMES.contains(name);
  }
}
