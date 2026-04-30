package com.github.idemura.cimple.common;

import com.github.idemura.cimple.compiler.Location;

public class StdErrConsumer implements ErrorConsumer {
  private int errorCount;

  public StdErrConsumer() {}

  @Override
  public void error(Location location, String message, Object... args) {
    var formattedMessage = message.formatted(args);
    System.err.printf("%s: %s%n", location == null ? "unknown" : location, formattedMessage);
    errorCount++;
  }

  public int getErrorCount() {
    return errorCount;
  }
}
