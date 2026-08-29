package io.lang.cimple.compiler;

public record SourceCode(String code, String fileName) {
  public SourceCode(String code) {
    this(code, null);
  }
}
