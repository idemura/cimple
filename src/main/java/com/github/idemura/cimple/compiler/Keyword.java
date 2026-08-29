package com.github.idemura.cimple.compiler;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Set;

public enum Keyword {
  BOOL("bool"),
  BREAK("break"),
  CASE("case"),
  CHAR("char"),
  CONST("const"),
  DEFER("defer"),
  DELETE("delete"),
  ELSE("else"),
  ENUM("enum", false),
  FALSE("false"),
  FLOAT32("float32"),
  FLOAT64("float64"),
  FOR("for"),
  FUNCTION("function"),
  IF("if"),
  IMPLEMENT("implement"),
  IMPORT("import"),
  INT("int"),
  INT32("int32"),
  INT64("int64"),
  INTERFACE("interface"),
  MATCH("match"),
  MODULE("module"),
  NEW("new"),
  NULL("null"),
  RETURN("return"),
  STRING("string"),
  STRUCT("struct", false),
  TEMPLATE("template"),
  TRUE("true"),
  TYPE("type"),
  UNION("union", false),
  VAR("var"),
  VOID("void");

  private final String symbolName;
  // There are global and context keywords.
  private final boolean global;

  Keyword(String symbolName) {
    this(symbolName, true);
  }

  Keyword(String symbolName, boolean global) {
    this.symbolName = symbolName;
    this.global = global;
  }

  private static final ImmutableMap<String, Keyword> ALL;

  static {
    var builder = new ImmutableMap.Builder<String, Keyword>();
    for (var keyword : values()) {
      builder.put(keyword.symbolName, keyword);
    }
    ALL = builder.build();
  }

  /// Returns null if not a keyword.
  public static Keyword fromString(String ident) {
    return ALL.get(ident);
  }

  @Override
  public String toString() {
    return symbolName;
  }

  private static final Set<String> RESERVED_NAMES;

  static {
    var builder = new ImmutableSet.Builder<String>();
    for (var keyword : values()) {
      if (keyword.global) {
        builder.add(keyword.symbolName);
      }
    }
    RESERVED_NAMES = builder.build();
  }

  public static boolean isReservedName(String name) {
    return RESERVED_NAMES.contains(name);
  }

  public static boolean isReservedTypeName(String name) {
    return ALL.containsKey(name);
  }
}
