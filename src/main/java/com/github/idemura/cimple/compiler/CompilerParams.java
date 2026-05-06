package com.github.idemura.cimple.compiler;

public interface CompilerParams {
  Appendable getDebugOutput();

  default boolean printTokens() {
    return false;
  }

  default boolean printAst() {
    return false;
  }
}
