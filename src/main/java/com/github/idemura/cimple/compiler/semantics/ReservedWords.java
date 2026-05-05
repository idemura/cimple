package com.github.idemura.cimple.compiler.semantics;

import com.google.common.collect.ImmutableSet;
import java.util.List;

class ReservedWords {
  private final ImmutableSet<String> reservedNames;

  ReservedWords(List<String> keywords) {
    this.reservedNames =
        new ImmutableSet.Builder<String>()
            .addAll(keywords)
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

  public boolean isReservedName(String name) {
    return reservedNames.contains(name);
  }

  public boolean isReservedTypeName(String name) {
    return reservedNames.contains(name);
  }
}
