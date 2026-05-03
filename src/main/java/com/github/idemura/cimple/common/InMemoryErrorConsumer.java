package com.github.idemura.cimple.common;

import com.github.idemura.cimple.compiler.Location;
import com.github.idemura.cimple.compiler.common.ErrorConsumer;
import java.util.ArrayList;
import java.util.List;

public class InMemoryErrorConsumer implements ErrorConsumer {
  private final List<Error> errors = new ArrayList<>();

  public InMemoryErrorConsumer() {}

  @Override
  public void error(Location location, String message, Object... args) {
    errors.add(new Error(location, message.formatted(args)));
  }

  @Override
  public List<Error> getErrors() {
    return errors;
  }
}
