package com.github.idemura.cimple.compiler;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

final class BuiltinTypes {
  static final TypeRef ANY = TypeRef.std("any");
  static final TypeRef VOID = TypeRef.std("void");
  static final TypeRef BOOL = TypeRef.std("bool");
  static final TypeRef BYTE = TypeRef.std("byte");
  static final TypeRef INT32 = TypeRef.std("int32");
  static final TypeRef INT64 = TypeRef.std("int64");
  static final TypeRef INT = TypeRef.std("int");
  static final TypeRef FLOAT32 = TypeRef.std("float32");
  static final TypeRef FLOAT64 = TypeRef.std("float64");
  static final TypeRef CHAR = TypeRef.std("char");
  static final TypeRef STRING = TypeRef.std("string");

  static final List<TypeRef> ALL =
      List.of(VOID, BOOL, INT32, INT64, FLOAT32, FLOAT64, CHAR, STRING);

  static final Map<String, TypeRef> NAME_MAP =
      ALL.stream().collect(Collectors.toMap(t -> t.name, t -> t));
}
