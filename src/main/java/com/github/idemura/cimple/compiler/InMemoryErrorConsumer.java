package com.github.idemura.cimple.compiler;

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
  public List<String> errors() {
    return errors;
  }
}
