package com.github.idemura.cimple.compiler;

import java.util.ArrayList;
import java.util.List;

class TokenStream {
  private final List<Token> tokens = new ArrayList<>();
  private int pos;

  TokenStream() {}

  List<Token> tokens() {
    return List.copyOf(tokens);
  }

  void add(Token token) {
    tokens.add(token);
  }

  boolean done() {
    return pos == tokens.size();
  }

  Token take() {
    return tokens.get(pos++);
  }

  Token take(TokenType type) {
    var token = tokens.get(pos);
    if (token.type() != type) {
      throw CompilerException.builder()
          .formatMessage("Expected token %s, got %s", type, token.toString())
          .setLocation(token.location())
          .build();
    }
    pos++;
    return token;
  }

  Token takeIf(TokenType tokenType) {
    var token = tokens.get(pos);
    if (token.type() != tokenType) {
      return null;
    }
    pos++;
    return token;
  }

  Token current() {
    return tokens.get(pos);
  }

  @Override
  public String toString() {
    return tokens.toString();
  }
}
