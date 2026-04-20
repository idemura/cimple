package com.github.idemura.cimple.compiler;

import com.github.idemura.cimple.common.StringOutput;

public interface CompilerParams {
  StringOutput getDebugOutput();

  default boolean printTokens() {
    return false;
  }

  default boolean printAst() {
    return false;
  }
}
