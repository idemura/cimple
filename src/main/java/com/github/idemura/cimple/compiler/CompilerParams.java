package com.github.idemura.cimple.compiler;

import com.github.idemura.cimple.common.IndentWriter;

public interface CompilerParams {
  IndentWriter getDebugOutput();

  default boolean printTokens() {
    return false;
  }

  default boolean printAst() {
    return false;
  }
}
