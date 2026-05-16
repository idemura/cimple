package com.github.idemura.cimple.compiler.semantics;

import com.google.common.collect.ImmutableSet;
import java.util.Set;

class ReservedWords {
  private final Set<String> reservedNames;
  private final Set<String> reservedTypeNames;

  ReservedWords(Set<String> reservedNames, Set<String> reservedTypeNames) {
    this.reservedNames = ImmutableSet.copyOf(reservedNames);
    this.reservedTypeNames = ImmutableSet.copyOf(reservedTypeNames);
  }

  public boolean isReservedName(String name) {
    return reservedNames.contains(name);
  }

  public boolean isReservedTypeName(String name) {
    return reservedTypeNames.contains(name);
  }
}
