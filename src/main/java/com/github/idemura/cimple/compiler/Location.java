package com.github.idemura.cimple.compiler;

public record Location(String file, int line, int column) {
  public Location(int line, int column) {
    this(null, line, column);
  }

  public boolean hasFile() {
    return file != null;
  }

  @Override
  public String toString() {
    return "%s[%d,%d]".formatted(file == null ? "" : file, line, column);
  }
}
