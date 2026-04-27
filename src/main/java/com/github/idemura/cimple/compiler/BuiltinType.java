package com.github.idemura.cimple.compiler;

public final class BuiltinType implements Type {
  public static final BuiltinType VOID = new BuiltinType("void");
  public static final BuiltinType NULL = new BuiltinType("null");
  public static final BuiltinType BOOL = new BuiltinType("bool");
  public static final BuiltinType BYTE = new BuiltinType("byte");
  public static final BuiltinType INT8 = new BuiltinType("int8");
  public static final BuiltinType INT16 = new BuiltinType("int16");
  public static final BuiltinType INT32 = new BuiltinType("int32");
  public static final BuiltinType INT64 = new BuiltinType("int64");
  public static final BuiltinType FLOAT32 = new BuiltinType("float32");
  public static final BuiltinType FLOAT64 = new BuiltinType("float64");
  public static final BuiltinType CHAR = new BuiltinType("char");
  public static final BuiltinType STRING = new BuiltinType("string");

  private final String name;

  private BuiltinType(String name) {
    this.name = name;
  }

  @Override
  public String getModuleName() {
    return "";
  }

  @Override
  public String getName() {
    return name;
  }
}
