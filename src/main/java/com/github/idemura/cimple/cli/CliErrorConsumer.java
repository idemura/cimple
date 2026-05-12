package com.github.idemura.cimple.cli;

import com.github.idemura.cimple.compiler.ErrorConsumer;

public class CliErrorConsumer extends ErrorConsumer {
  private int errorCount;

  public CliErrorConsumer() {}

  @Override
  public void outputError(String message) {
    System.err.println(message);
    errorCount++;
  }

  @Override
  public int errorCount() {
    return errorCount;
  }
}
