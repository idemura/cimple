package com.github.idemura.cimple.compiler;

public record QualifiedName(String moduleName, String name) {
  public QualifiedName(String name) {
    this(null, name);
  }

  public QualifiedName withModuleName(String moduleName) {
    return new QualifiedName(moduleName, name);
  }

  @Override
  public String toString() {
    if (moduleName == null || moduleName.isEmpty()) {
      return name;
    }
    return moduleName + "::" + name;
  }
}
