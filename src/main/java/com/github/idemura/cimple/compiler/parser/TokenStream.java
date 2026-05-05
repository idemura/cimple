package com.github.idemura.cimple.compiler.parser;

import com.github.idemura.cimple.compiler.CompilerException;
import java.util.ArrayList;
import java.util.List;

public class TokenStream {
  private final List<Token> tokens = new ArrayList<>();
  private int pos;

  public TokenStream() {}

  public List<Token> tokens() {
    return List.copyOf(tokens);
  }

  public void add(Token token) {
    tokens.add(token);
  }

  public boolean done() {
    return pos == tokens.size();
  }

  public Token take() {
    checkPosition();
    return tokens.get(pos++);
  }

  public Token take(TokenType type) {
    checkPosition();
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

  public boolean takeIf(TokenType type) {
    var token = tokens.get(pos);
    if (token.type() != type) {
      return false;
    }
    pos++;
    return true;
  }

  Token current() {
    return tokens.get(pos);
  }

  Token next() {
    if (pos + 1 < tokens.size()) {
      return tokens.get(pos + 1);
    } else {
      throw new CompilerException("Reached unexpected EOF", null);
    }
  }

  @Override
  public String toString() {
    return tokens.toString();
  }

  private void checkPosition() {
    if (pos == tokens.size()) {
      throw CompilerException.builder().formatMessage("Reached token stream end").build();
    }
  }
}
