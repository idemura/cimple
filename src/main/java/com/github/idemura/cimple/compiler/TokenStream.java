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

  boolean takeIf(TokenType type) {
    var token = tokens.get(pos);
    if (token.type() != type) {
      return false;
    }
    pos++;
    return true;
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

  boolean takeKeywordIf(TokenType type) {
    var token = tokens.get(pos);
    var kw = token.keyword();
    if (kw != type) {
      return false;
    }
    pos++;
    return true;
  }

  Token current() {
    return tokens.get(pos);
  }

  TokenType next() {
    if (pos + 1 < tokens.size()) {
      return tokens.get(pos + 1).type();
    } else {
      return TokenType.EOF;
    }
  }

  @Override
  public String toString() {
    return tokens.toString();
  }
}
