package com.github.idemura.cimple.compiler.entity;

import com.github.idemura.cimple.compiler.QualifiedName;

public class BuiltinType extends Type {
  public static final BuiltinType INT32 = new BuiltinType(QualifiedName.ofBuiltin("int32"));
  public static final BuiltinType INT64 = new BuiltinType(QualifiedName.ofBuiltin("int64"));

  private BuiltinType(QualifiedName name) {
    super(name);
  }
}
