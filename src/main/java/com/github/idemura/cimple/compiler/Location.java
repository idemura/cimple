package com.github.idemura.cimple.compiler;

public record Location(String file, int line, int column) {
  public boolean isNone() {
    return file.isEmpty();
  }

  @Override
  public String toString() {
    if (file.isEmpty()) {
      return "";
    }
    return file + ":" + line + "," + column;
  }
}
