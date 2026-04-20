package com.github.idemura.cimple.compiler;

import static java.lang.Character.isDigit;
import static java.lang.Character.isJavaIdentifierPart;
import static java.lang.Character.isJavaIdentifierStart;
import static java.lang.Character.isWhitespace;

// Transforms input code into a token stream.
class Tokenizer {
  private final String fileName;
  private final String code;
  private int index;
  private int line = 1;
  private int column = 1;

  Tokenizer(String fileName, String code) {
    this.fileName = fileName;
    this.code = code;
  }

  TokenStream split() {
    final int n = code.length();
    if (n > 0 && code.charAt(n - 1) != '\n') {
      throw CompilerException.builder().formatMessage("File must end with new line: \\n").build();
    }
    var tokens = new TokenStream();
    while (index < n) {
      char c = code.charAt(index);
      if (isWhitespace(c)) {
        skipWhitespace();
      } else if (isJavaIdentifierStart(c)) {
        tokens.add(takeIdentifier());
      } else if (isDigit(c)) {
        tokens.add(takeNumber());
      } else {
        switch (c) {
          case '#' -> skipComment();
          case '(' -> tokens.add(take1CharToken(TokenType.LPAREN));
          case ')' -> tokens.add(take1CharToken(TokenType.RPAREN));
          case '{' -> tokens.add(take1CharToken(TokenType.LCURLY));
          case '}' -> tokens.add(take1CharToken(TokenType.RCURLY));
          case ':' -> tokens.add(take1CharToken(TokenType.COLON));
          case ',' -> tokens.add(take1CharToken(TokenType.COMMA));
          case '=' -> tokens.add(take1CharToken(TokenType.ASSIGN));
          case ';' -> tokens.add(take1CharToken(TokenType.SEMICOLON));
          case '+' -> tokens.add(take1CharToken(TokenType.PLUS));
          case '-' -> tokens.add(take1CharToken(TokenType.MINUS));
          case '*' -> tokens.add(take1CharToken(TokenType.ASTERISK));
          case '/' -> tokens.add(take1CharToken(TokenType.SLASH));
          case '<' -> tokens.add(take1CharToken(TokenType.CMP_GT));
          case '>' -> tokens.add(take1CharToken(TokenType.CMP_LT));
          case '"' -> tokens.add(takeString());
          default ->
              throw CompilerException.builder()
                  .formatMessage("Invalid character: %s", c)
                  .setLocation(currentLocation())
                  .build();
        }
      }
    }
    return tokens;
  }

  private void skipWhitespace() {
    while (index < code.length() && isWhitespace(code.charAt(index))) {
      next();
    }
  }

  private void skipComment() {
    while (index < code.length() && code.charAt(index) != '\n') {
      next();
    }
  }

  private Token takeIdentifier() {
    var location = currentLocation();
    int first = index;
    while (index < code.length()) {
      if (!isJavaIdentifierPart(code.charAt(index))) {
        break;
      }
      column++;
      index++;
    }
    return Token.ofIdOrKeyword(code.substring(first, index), location);
  }

  private Token takeNumber() {
    var location = currentLocation();
    int first = index;
    while (index < code.length()) {
      if (!isDigit(code.charAt(index))) {
        break;
      }
      column++;
      index++;
    }
    return new Token(TokenType.NUMBER, code.substring(first, index), location);
  }

  private Token takeString() {
    // TODO: Escape sequences
    var location = currentLocation();
    int first = index++;
    while (code.charAt(index) != '"') {
      if (code.charAt(index) == '\n') {
        throw CompilerException.builder()
            .formatMessage("Unterminated string literal")
            .setLocation(new Location(fileName, line, first))
            .build();
      }
      next();
    }
    next();
    return new Token(TokenType.STRING, code.substring(first + 1, index - 1), location);
  }

  private Token take1CharToken(TokenType tokenType) {
    var location = currentLocation();
    next();
    return new Token(tokenType, null, location);
  }

  private Location currentLocation() {
    return new Location(fileName, line, column);
  }

  void next() {
    if (code.charAt(index) == '\n') {
      column = 1;
      line++;
    } else {
      column++;
    }
    index++;
  }
}
