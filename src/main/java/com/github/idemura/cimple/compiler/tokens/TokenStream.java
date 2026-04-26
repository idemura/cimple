package com.github.idemura.cimple.compiler.tokens;

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
    return tokens.get(pos++);
  }

  public Token take(TokenType type) {
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

  public Token takeKeyword(TokenType type) {
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

  public boolean takeKeywordIf(TokenType type) {
    var token = tokens.get(pos);
    var kw = token.keyword();
    if (kw != type) {
      return false;
    }
    pos++;
    return true;
  }

  public Token current() {
    return tokens.get(pos);
  }

  public TokenType next() {
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
