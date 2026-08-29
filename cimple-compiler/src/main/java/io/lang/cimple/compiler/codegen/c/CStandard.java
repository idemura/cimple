package io.lang.cimple.compiler.codegen.c;

public enum CStandard {
  C11,
  C17,
  C23;

  public static CStandard def() {
    return CStandard.C23;
  }
}
