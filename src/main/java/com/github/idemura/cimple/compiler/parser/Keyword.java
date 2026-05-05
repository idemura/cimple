package com.github.idemura.cimple.compiler.parser;

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.List;

public enum Keyword {
  CASE("case"),
  CLASS("class"),
  CONST("const"),
  DEFER("defer"),
  ELSE("else"),
  FOR("for"),
  FUNCTION("function"),
  GOTO("goto"),
  IF("if"),
  IMPLEMENT("implement"),
  IMPORT("import"),
  INTERFACE("interface"),
  MATCH("match"),
  MODULE("module"),
  RETURN("return"),
  RECORD("record"),
  TEMPLATE("template"),
  TYPE("type"),
  UNION("union"),
  VAR("var");

  private static final ImmutableMap<String, Keyword> SYMBOL_NAME_MAP = createSymbolNameMap();

  private final String symbolName;

  Keyword(String symbolName) {
    this.symbolName = symbolName;
  }

  public static Keyword find(String ident) {
    return SYMBOL_NAME_MAP.get(ident);
  }

  public String symbolName() {
    return symbolName;
  }

  public static ImmutableMap<String, Keyword> createSymbolNameMap() {
    var builder = new ImmutableMap.Builder<String, Keyword>();
    for (var keyword : values()) {
      builder.put(keyword.symbolName(), keyword);
    }
    return builder.build();
  }

  public static List<String> valueList() {
    return Arrays.stream(Keyword.values()).map(Keyword::symbolName).toList();
  }
}
