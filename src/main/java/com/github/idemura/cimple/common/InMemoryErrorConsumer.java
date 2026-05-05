package com.github.idemura.cimple.common;

import com.github.idemura.cimple.compiler.ErrorConsumer;
import com.github.idemura.cimple.compiler.Location;
import java.util.ArrayList;
import java.util.List;

public class InMemoryErrorConsumer extends ErrorConsumer {
  private final List<String> errors = new ArrayList<>();

  public InMemoryErrorConsumer() {}

  @Override
  public void outputError(String message) {
    errors.add(message);
  }

  @Override
  public void errorAt(Location location, String pattern, Object... args) {
    errors.add(formatError(ERROR, location, pattern, args));
  }

  @Override
  public List<String> getErrors() {
    return errors;
  }
}
