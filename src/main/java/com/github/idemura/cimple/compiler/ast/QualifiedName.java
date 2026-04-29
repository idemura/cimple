package com.github.idemura.cimple.compiler.ast;

public record QualifiedName(String moduleName, String name) {
  public QualifiedName(String name) {
    this(null, name);
  }

  @Override
  public String toString() {
    if (moduleName == null || moduleName.isEmpty()) {
      return name;
    }
    return moduleName + "." + name;
  }
}
