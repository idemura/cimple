package com.github.idemura.cimple.compiler;

import java.util.List;

public abstract class ErrorConsumer {
  public static final String FATAL = "fatal";
  public static final String ERROR = "error";

  public enum Mode {
    PRINT_LOCATION(0x1L),
    PRINT_LEVEL(0x2L),
    THROW_ON_ERROR(0x4L);

    private final long bit;

    Mode(long bit) {
      this.bit = bit;
    }
  }

  protected long mode;
  protected int errorCount;

  protected ErrorConsumer() {}

  protected abstract void outputError(String message);

  public void enable(Mode mode) {
    this.mode |= mode.bit;
  }

  public void disable(Mode mode) {
    this.mode &= ~mode.bit;
  }

  public int errorCount() {
    return errorCount;
  }

  public void error(String pattern, Object... args) {
    processAndOutputError(formatError(ERROR, null, pattern, args));
  }

  public void errorAt(Location location, String pattern, Object... args) {
    processAndOutputError(formatError(ERROR, location, pattern, args));
  }

  public CompilerException fatal(String pattern, Object... args) {
    var message = formatError(FATAL, null, pattern, args);
    processAndOutputError(message);
    return new CompilerException(message);
  }

  public CompilerException fatalAt(Location location, String pattern, Object... args) {
    var message = formatError(FATAL, location, pattern, args);
    processAndOutputError(message);
    return new CompilerException(message);
  }

  protected String formatError(String level, Location location, String pattern, Object... args) {
    var sb = new StringBuilder();
    if (checkMode(Mode.PRINT_LEVEL)) {
      sb.append(level).append(": ");
    }
    if (location != null && checkMode(Mode.PRINT_LOCATION)) {
      sb.append(location).append(": ");
    }
    sb.append(pattern.formatted(args));
    return sb.toString();
  }

  protected boolean checkMode(Mode mode) {
    return (this.mode & mode.bit) != 0;
  }

  private void processAndOutputError(String message) {
    errorCount++;
    if (checkMode(Mode.THROW_ON_ERROR)) {
      throw new CompilerException(message);
    }
    outputError(message);
  }

  // Returns collected diagnostics when the implementation stores them in memory.
  public List<String> errors() {
    throw new UnsupportedOperationException();
  }
}
