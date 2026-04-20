package com.github.idemura.cimple.compiler;

record Token(TokenType type, String value, Location location) {
  static Token ofIdOrKeyword(String value, Location location) {
    var kw = TokenType.ofKeyword(value);
    if (kw == null) {
      return new Token(TokenType.IDENTIFIER, value, location);
    } else {
      return new Token(kw, null, location);
    }
  }

  Token(TokenType type, Location location) {
    this(type, null, location);
  }

  boolean is(TokenType type) {
    return this.type == type;
  }

  @Override
  public int hashCode() {
    return 1009 * type.hashCode() + (value == null ? 0 : value.hashCode());
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
