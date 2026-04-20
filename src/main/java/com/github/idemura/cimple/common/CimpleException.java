package com.github.idemura.cimple.common;

public class CimpleException extends RuntimeException {
  public static CimpleException of(Exception cause) {
    if (cause instanceof CimpleException e) {
      return e;
    } else {
      return new CimpleException("Wrap exception", cause);
    }
  }

  public CimpleException(String message) {
    super(message);
  }

  public CimpleException(String message, Throwable cause) {
    super(message, cause);
  }
}
