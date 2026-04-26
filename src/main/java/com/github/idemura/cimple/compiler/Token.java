package com.github.idemura.cimple.compiler;

record Token(TokenType type, String value, Location location) {
  Token(TokenType type) {
    this(type, null, null);
  }

  Token(TokenType type, String value) {
    this(type, value, null);
  }

  TokenType keyword() {
    if (value == null) {
      return type;
    }
    var keyword = TokenType.ofKeyword(value);
    if (keyword == null) {
      return type;
    }
    return keyword;
  }

  boolean is(TokenType type) {
    return this.type == type;
  }

  @Override
  public String toString() {
    if (value == null) {
      return "%s@%s".formatted(type.printableName(), location);
    } else {
      return "%s(%s)@%s".formatted(type.printableName(), value, location);
    }
  }
}
