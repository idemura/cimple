package com.github.idemura.cimple.compiler.tokens;

import com.github.idemura.cimple.compiler.CompilerException;
import com.github.idemura.cimple.compiler.Location;
import com.github.idemura.cimple.compiler.common.Keyword;

public record Token(TokenType type, String value, Location location) {
  public Token(TokenType type) {
    this(type, null, null);
  }

  public Token(TokenType type, String value) {
    this(type, value, null);
  }

  public Keyword keywordOrNull() {
    if (type != TokenType.IDENTIFIER) {
      return null;
    }
    var keyword = Keyword.symbolNameMap().get(value);
    if (keyword == null) {
      return null;
    }
    return keyword;
  }

  public Keyword keyword() {
    var keyword = keywordOrNull();
    if (keyword == null) {
      throw CompilerException.builder()
          .formatMessage("Expected keyword, found %s", type.symbolName())
          .setLocation(location)
          .build();
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
