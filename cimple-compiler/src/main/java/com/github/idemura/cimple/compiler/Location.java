package com.github.idemura.cimple.compiler;

public record Location(String fileName, int line, int column) {
  public Location(int line, int column) {
    this(null, line, column);
  }

  public boolean hasFile() {
    return fileName != null;
  }

  @Override
  public String toString() {
    if (fileName == null) {
      return "%d,%d".formatted(line, column);
    } else {
      return "%s@%d,%d".formatted(fileName, line, column);
    }
  }
}
