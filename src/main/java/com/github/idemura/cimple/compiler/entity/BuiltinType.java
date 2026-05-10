package com.github.idemura.cimple.compiler.entity;

import com.github.idemura.cimple.compiler.QualifiedName;

public class BuiltinType extends Type {
  public static final BuiltinType INT32 = new BuiltinType(QualifiedName.ofType("int32").builtin());
  public static final BuiltinType INT64 = new BuiltinType(QualifiedName.ofType("int64").builtin());

  private BuiltinType(QualifiedName name) {
    super(name);
  }
}
