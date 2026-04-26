package com.github.idemura.cimple.compiler;

final class BuiltinTypeRefs {
  static final TypeRef VOID = new TypeRef("void");
  static final TypeRef BOOL = new TypeRef("bool");
  static final TypeRef BYTE = new TypeRef("byte");
  static final TypeRef INT32 = new TypeRef("int32");
  static final TypeRef INT64 = new TypeRef("int64");
  static final TypeRef INT = new TypeRef("int"); // Alias to int64
  static final TypeRef FLOAT32 = new TypeRef("float32");
  static final TypeRef FLOAT64 = new TypeRef("float64");
  static final TypeRef CHAR = new TypeRef("char");
  static final TypeRef STRING = new TypeRef("string");
}
