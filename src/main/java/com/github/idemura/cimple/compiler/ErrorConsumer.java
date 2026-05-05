package com.github.idemura.cimple.compiler;

import java.util.List;

public abstract class ErrorConsumer {
  public static final String ERROR = "error";

  public enum Mode {
    LOCATION(0x1L),
    LEVEL(0x2L);

    private final long bit;

    Mode(long bit) {
      this.bit = bit;
    }
  }

  protected long mode;

  protected ErrorConsumer() {
    this(0);
  }

  protected ErrorConsumer(long mode) {
    this.mode = mode;
  }

  public abstract void outputError(String message);

  public void enable(Mode mode) {
    this.mode |= mode.bit;
  }

  public void disable(Mode mode) {
    this.mode &= ~mode.bit;
  }

  public void error(String pattern, Object... args) {
    outputError(formatError(ERROR, null, pattern, args));
  }

  public void errorAt(Location location, String pattern, Object... args) {
    outputError(formatError(ERROR, location, pattern, args));
  }

  protected String formatError(String level, Location location, String pattern, Object... args) {
    var sb = new StringBuilder();
    if (checkMode(Mode.LEVEL) && level != null) {
      sb.append(level).append(": ");
    }
    if (checkMode(Mode.LOCATION) && location != null) {
      sb.append(location).append(": ");
    }
    sb.append(pattern.formatted(args));
    return sb.toString();
  }

  protected boolean checkMode(Mode mode) {
    return (this.mode & mode.bit) != 0;
  }

  /// May throw if not supported.
  public List<String> getErrors() {
    throw new UnsupportedOperationException();
  }
}
