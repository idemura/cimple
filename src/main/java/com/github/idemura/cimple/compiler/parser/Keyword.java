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
  RECORD("record") /* not reserved */,
  RETURN("return"),
  TEMPLATE("template"),
  TYPE("type"),
  UNION("union") /* not reserved */,
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
      // record/union are contextual after "type"; elsewhere they are ordinary identifiers.
      if (keyword == RECORD || keyword == UNION) {
        continue;
      }
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

  // Builtin types that we allow to define methods for.
  public static Set<String> reservedTypeNames() {
    var builder = new ImmutableSet.Builder<String>();
    return builder
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
        .build();
  }
}
