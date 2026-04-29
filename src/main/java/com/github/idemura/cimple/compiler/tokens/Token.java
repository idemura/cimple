package com.github.idemura.cimple.compiler.tokens;

import com.github.idemura.cimple.compiler.Location;

public record Token(TokenType type, String value, Location location) {
  public Token(TokenType type) {
    this(type, null, null);
  }

  public Token(TokenType type, String value) {
    this(type, value, null);
  }

  public TokenType keyword() {
    if (value == null) {
      return type;
    }
    var keyword = TokenType.ofKeyword(value);
    if (keyword == null) {
      return type;
    }
    return keyword;
  }

  public boolean is(TokenType type) {
    return this.type == type;
  }

  @Override
  public String toString() {
    if (value == null) {
      return "%s@%s".formatted(type.symbolName(), location);
    } else {
      return "%s(%s)@%s".formatted(type.symbolName(), value, location);
    }
  }
}
