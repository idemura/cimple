package com.github.idemura.cimple.compiler.common;

import com.google.common.collect.ImmutableMap;
import java.util.Map;

public enum Keyword {
  ALIAS("alias"),
  CASE("case"),
  CLASS("class"),
  CONST("const"),
  DEFER("defer"),
  IF("if"),
  UNION("union"),
  ELSE("else"),
  FOR("for"),
  FUNCTION("function"),
  GOTO("goto"),
  IMPLEMENT("implement"),
  IMPORT("import"),
  INTERFACE("interface"),
  MATCH("match"),
  MODULE("module"),
  RETURN("return"),
  RECORD("record"),
  TEMPLATE("template"),
  TYPE("type"),
  VAR("var");

  private static final ImmutableMap<String, Keyword> SYMBOL_NAME_MAP = createSymbolNameMap();

  private final String symbolName;

  Keyword(String symbolName) {
    this.symbolName = symbolName;
  }

  public static Map<String, Keyword> symbolNameMap() {
    return SYMBOL_NAME_MAP;
  }

  public static Keyword of(String symbolName) {
    var keyword = SYMBOL_NAME_MAP.get(symbolName);
    if (keyword != null) {
      return keyword;
    }
    throw new IllegalArgumentException("Unknown keyword: %s".formatted(symbolName));
  }

  public static ImmutableMap<String, Keyword> createSymbolNameMap() {
    var builder = new ImmutableMap.Builder<String, Keyword>();
    for (var keyword : values()) {
      builder.put(keyword.symbolName(), keyword);
    }
    return builder.build();
  }

  public String symbolName() {
    return symbolName;
  }
}
