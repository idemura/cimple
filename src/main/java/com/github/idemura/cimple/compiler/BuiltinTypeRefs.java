package com.github.idemura.cimple.compiler;

public final class BuiltinTypeRefs {
  public static final TypeRef VOID = new TypeRef("void");
  public static final TypeRef BOOL = new TypeRef("bool");
  public static final TypeRef BYTE = new TypeRef("byte");
  public static final TypeRef INT32 = new TypeRef("int32");
  public static final TypeRef INT64 = new TypeRef("int64");
  public static final TypeRef INT = new TypeRef("int"); // Alias to int64
  public static final TypeRef FLOAT32 = new TypeRef("float32");
  public static final TypeRef FLOAT64 = new TypeRef("float64");
  public static final TypeRef CHAR = new TypeRef("char");
  public static final TypeRef STRING = new TypeRef("string");
}
