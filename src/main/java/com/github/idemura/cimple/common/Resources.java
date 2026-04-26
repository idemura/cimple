package com.github.idemura.cimple.common;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

public final class Resources {
  private Resources() {}

  public static String readResource(Class<?> clazz, String name) {
    try (var stream = clazz.getResourceAsStream(name)) {
      if (stream == null) {
        throw new IOException(
            "Resource not found: %s (class: %s)".formatted(name, clazz.getName()));
      }
      return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new UncheckedIOException(
          "Error reading resource: %s (class: %s)".formatted(name, clazz.getName()), e);
    }
  }
}
