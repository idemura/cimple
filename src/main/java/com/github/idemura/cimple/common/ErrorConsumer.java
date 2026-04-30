package com.github.idemura.cimple.common;

import com.github.idemura.cimple.compiler.Location;

public interface ErrorConsumer {
  void error(Location location, String message, Object... args);
}
