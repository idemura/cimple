package com.github.idemura.cimple.compiler.tokens;

import static com.github.idemura.cimple.compiler.tokens.TokenType.*;
import static java.lang.Character.isDigit;
import static java.lang.Character.isJavaIdentifierPart;
import static java.lang.Character.isJavaIdentifierStart;
import static java.lang.Character.isWhitespace;

import com.github.idemura.cimple.compiler.CompilerException;
import com.github.idemura.cimple.compiler.Location;

// Transforms input code into a token stream.
public class Tokenizer {
  private final String fileName;
  private final String code;
  private int index;
  private int line = 1;
  private int column = 1;

  public Tokenizer(String fileName, String code) {
    this.fileName = fileName;
    this.code = code;
  }

  public TokenStream split() {
    final int n = code.length();
    if (n > 0 && code.charAt(n - 1) != '\n') {
      throw CompilerException.builder().formatMessage("File must end with new line").build();
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
          case '(' -> tokens.add(take1CharToken(LPAREN));
          case ')' -> tokens.add(take1CharToken(RPAREN));
          case '{' -> tokens.add(take1CharToken(LCURLY));
          case '}' -> tokens.add(take1CharToken(RCURLY));
          case ':' -> tokens.add(take1CharToken(COLON));
          case ',' -> tokens.add(take1CharToken(COMMA));
          case '=' -> tokens.add(take1CharToken(ASSIGN));
          case ';' -> tokens.add(take1CharToken(SEMICOLON));
          case '+' -> tokens.add(take1CharToken(PLUS));
          case '-' -> tokens.add(take1CharToken(MINUS));
          case '*' -> tokens.add(take1CharToken(ASTERISK));
          case '/' -> tokens.add(take1CharToken(SLASH));
          case '<' -> tokens.add(take1CharToken(CMP_GT));
          case '>' -> tokens.add(take1CharToken(CMP_LT));
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
    return new Token(IDENTIFIER, code.substring(first, index), location);
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
    return new Token(NUMBER, code.substring(first, index), location);
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
    return new Token(STRING, code.substring(first + 1, index - 1), location);
  }

  private Token take1CharToken(TokenType tokenType) {
    var location = currentLocation();
    next();
    return new Token(tokenType, null, location);
  }

  private Location currentLocation() {
    return new Location(fileName, line, column);
  }

  private void next() {
    if (code.charAt(index) == '\n') {
      column = 1;
      line++;
    } else {
      column++;
    }
    index++;
  }
}
