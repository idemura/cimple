package com.github.idemura.cimple.compiler;

public interface CompilerParams {
  Appendable debugOutput();

  default boolean printTokens() {
    return false;
  }

  default boolean printAst() {
    return false;
  }
}
