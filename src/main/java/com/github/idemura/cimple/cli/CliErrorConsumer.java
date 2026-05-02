package com.github.idemura.cimple.cli;

import com.github.idemura.cimple.compiler.Location;
import com.github.idemura.cimple.compiler.common.ErrorConsumer;

public class CliErrorConsumer implements ErrorConsumer {
  private int errorCount;

  public CliErrorConsumer() {}

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
