package com.github.idemura.cimple.compiler.ast;

import java.util.Objects;

public final class QualifiedName {
  private final String name;
  private String moduleName;

  public QualifiedName(String name) {
    this.name = name;
  }

  public QualifiedName(String moduleName, String name) {
    this.name = name;
    this.moduleName = moduleName;
  }

  public String name() {
    return name;
  }

  public String getModuleName() {
    return moduleName;
  }

  public void setModuleName(String moduleName) {
    this.moduleName = moduleName;
  }

  @Override
  public int hashCode() {
    return Objects.hash(moduleName, name);
  }

  @Override
  public boolean equals(Object object) {
    return this == object
        || (object instanceof QualifiedName other
            && Objects.equals(moduleName, other.moduleName)
            && Objects.equals(name, other.name));
  }

  @Override
  public String toString() {
    if (moduleName == null || moduleName.isEmpty()) {
      return name;
    }
    return moduleName + "::" + name;
  }
}
