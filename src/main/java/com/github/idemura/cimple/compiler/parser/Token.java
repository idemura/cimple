package com.github.idemura.cimple.compiler.parser;

import com.github.idemura.cimple.compiler.Location;

public record Token(TokenType type, String value, Location location) {
  public Token(TokenType type) {
    this(type, null, null);
  }

  public Token(TokenType type, String value) {
    this(type, value, null);
  }

  public boolean is(TokenType type) {
    return this.type == type;
  }

  @Override
  public String toString() {
    if (value == null) {
      return type.symbolName();
    } else {
      return "%s(%s)".formatted(type.symbolName(), value);
    }
  }
}
