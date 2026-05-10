package com.github.idemura.cimple.compiler.entity;

import com.github.idemura.cimple.compiler.Identifier;

public class BuiltinType extends Type {
  public static final BuiltinType INT32 = new BuiltinType(Identifier.ofType("int32").builtin());
  public static final BuiltinType INT64 = new BuiltinType(Identifier.ofType("int64").builtin());

  private BuiltinType(Identifier name) {
    super(name);
  }
}
