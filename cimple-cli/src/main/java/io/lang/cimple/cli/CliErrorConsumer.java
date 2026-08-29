package io.lang.cimple.cli;

import io.lang.cimple.compiler.ErrorConsumer;

public class CliErrorConsumer extends ErrorConsumer {
  public CliErrorConsumer() {}

  @Override
  protected void outputError(String message) {
    // Do not call super method, do not store error in memory.
    System.err.println(message);
  }
}
