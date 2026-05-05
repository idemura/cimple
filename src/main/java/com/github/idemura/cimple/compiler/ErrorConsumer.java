package com.github.idemura.cimple.compiler;

import java.util.List;

public interface ErrorConsumer {
  record Error(Location location, String message) {}

  void error(Location location, String message, Object... args);

  /// May throw if not supported.
  List<Error> getErrors();
}
