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

  Token takeIf(TokenType type) {
    var token = tokens.get(pos);
    if (token.type() != type) {
      return null;
    }
    pos++;
    return token;
  }

  Token takeKeyword(TokenType type) {
    var token = tokens.get(pos);
    var kw = token.keyword();
    if (kw != type) {
      throw CompilerException.builder()
          .formatMessage("Expected token %s, got %s", type, token)
          .setLocation(token.location())
          .build();
    }
    pos++;
    return new Token(type, token.value(), token.location());
  }

  Token takeKeywordIf(TokenType type) {
    var token = tokens.get(pos);
    var kw = token.keyword();
    if (kw != type) {
      return null;
    }
    pos++;
    return new Token(type, token.value(), token.location());
  }

  Token current() {
    return tokens.get(pos);
  }

  @Override
  public String toString() {
    return tokens.toString();
  }
}
