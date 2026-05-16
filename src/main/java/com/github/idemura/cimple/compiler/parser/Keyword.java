package com.github.idemura.cimple.compiler.parser;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Set;

public enum Keyword {
  CASE("case"),
  CLASS("class"),
  CONST("const"),
  DEFER("defer"),
  DELETE("delete"),
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
  NEW("new"),
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

  @Override
  public String toString() {
    return symbolName;
  }

  public static ImmutableMap<String, Keyword> createSymbolNameMap() {
    var builder = new ImmutableMap.Builder<String, Keyword>();
    for (var keyword : values()) {
      builder.put(keyword.symbolName, keyword);
    }
    return builder.build();
  }

  public static Set<String> reservedNames() {
    var builder = new ImmutableSet.Builder<String>();
    for (var keyword : values()) {
      builder.add(keyword.symbolName);
    }
    return builder
        .add("true")
        .add("false")
        .add("null")
        .add("bool")
        .add("byte")
        .add("char")
        .add("float")
        .add("float32")
        .add("float64")
        .add("int")
        .add("int32")
        .add("int64")
        .add("string")
        .add("void")
        .build();
  }

  public static Set<String> reservedTypeNames() {
    return reservedNames();
  }
}
