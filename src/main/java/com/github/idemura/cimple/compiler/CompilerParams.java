package com.github.idemura.cimple.compiler;

public class CompilerParams {
  public Appendable debugOutput() {
    return System.err;
  }

  public boolean printTokens() {
    return false;
  }

  public boolean printAst() {
    return false;
  }

  public int indent() {
    return 2;
  }
}
