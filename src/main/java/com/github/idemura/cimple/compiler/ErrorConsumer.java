package com.github.idemura.cimple.compiler;

import java.util.ArrayList;
import java.util.List;

public class ErrorConsumer {
  public static final String FATAL = "Fatal";
  public static final String ERROR = "Error";

  public enum Mode {
    PRINT_LOCATION(0x1),
    PRINT_LEVEL(0x2),
    THROW_ON_ERROR(0x4);

    private final long bit;

    Mode(long bit) {
      this.bit = bit;
    }
  }

  private final List<String> errors = new ArrayList<>();
  private long mode;
  private int errorCount;

  public ErrorConsumer() {}

  protected void outputError(String message) {
    errors.add(message);
  }

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
    errorAt(null, pattern, args);
  }

  public void errorAt(Location location, String pattern, Object... args) {
    processError(formatError(ERROR, location, pattern, args));
  }

  public CompilerException fatal(String pattern, Object... args) {
    return fatalAt(null, pattern, args);
  }

  public CompilerException fatalAt(Location location, String pattern, Object... args) {
    var message = formatError(FATAL, location, pattern, args);
    processError(message);
    return new CompilerException(message);
  }

  public String formatError(String level, Location location, String pattern, Object... args) {
    var sb = new StringBuilder();
    if (modeIs(Mode.PRINT_LEVEL)) {
      sb.append(level).append(": ");
    }
    if (modeIs(Mode.PRINT_LOCATION) && location != null) {
      sb.append(location).append(": ");
    }
    sb.append(pattern.formatted(args));
    return sb.toString();
  }

  private boolean modeIs(Mode mode) {
    return (this.mode & mode.bit) != 0;
  }

  private void processError(String message) {
    errorCount++;
    if (modeIs(Mode.THROW_ON_ERROR)) {
      throw new CompilerException(message);
    } else {
      outputError(message);
    }
  }

  public List<String> errors() {
    return errors;
  }
}
