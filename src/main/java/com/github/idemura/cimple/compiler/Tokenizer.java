package com.github.idemura.cimple.compiler;

import java.util.ArrayList;

// Transforms input code into a token stream.
class Tokenizer {
  private final CompilerParams params;

  Tokenizer(CompilerParams params) {
    this.params = params;
  }

  TokenStream split(String fileName, String code) {
    final int n = code.length();
    if (code.length() > 0 && code.charAt(n - 1) != '\n') {
      throw CompilerException.builder().formatMessage("File must end with new line: \\n").build();
    }

    var tokens = new ArrayList<Token>();
    int line = 1;
    int column = 1;
    for (int i = 0; i < n; ) {
      char c = code.charAt(i);
      if (Character.isWhitespace(c)) {
        if (c == '\n') {
          line++;
          column = 1;
        } else {
          column++;
        }
        i++;
      } else if (Character.isJavaIdentifierStart(c)) {
        var location = new Location(fileName, line, column);
        int first = i;
        while (i < n) {
          if (!Character.isJavaIdentifierPart(code.charAt(i))) {
            break;
          }
          column++;
          i++;
        }
        tokens.add(Token.ofIdOrKeyword(code.substring(first, i), location));
      } else if (Character.isDigit(c)) {
        var location = new Location(fileName, line, column);
        int first = i;
        while (i < n) {
          if (!Character.isDigit(code.charAt(i))) {
            break;
          }
          column++;
          i++;
        }
        tokens.add(new Token(TokenType.NUMBER, code.substring(first, i), location));
      } else {
        var location = new Location(fileName, line, column);
        int move = 0;
        switch (c) {
          case '#' -> {
            while (i < n && code.charAt(i) != '\n') {
              column++;
              i++;
            }
          }
          case '(' -> {
            tokens.add(new Token(TokenType.LPAREN, location));
            move = 1;
          }
          case ')' -> {
            tokens.add(new Token(TokenType.RPAREN, location));
            move = 1;
          }
          case '{' -> {
            tokens.add(new Token(TokenType.LCURLY, location));
            move = 1;
          }
          case '}' -> {
            tokens.add(new Token(TokenType.RCURLY, location));
            move = 1;
          }
          case ':' -> {
            tokens.add(new Token(TokenType.COLON, location));
            move = 1;
          }
          case ',' -> {
            tokens.add(new Token(TokenType.COMMA, location));
            move = 1;
          }
          case '=' -> {
            tokens.add(new Token(TokenType.ASSIGN, location));
            move = 1;
          }
          case ';' -> {
            tokens.add(new Token(TokenType.SEMICOLON, location));
            move = 1;
          }
          case '+' -> {
            tokens.add(new Token(TokenType.PLUS, location));
            move = 1;
          }
          case '-' -> {
            tokens.add(new Token(TokenType.MINUS, location));
            move = 1;
          }
          case '*' -> {
            tokens.add(new Token(TokenType.ASTERISK, location));
            move = 1;
          }
          case '/' -> {
            tokens.add(new Token(TokenType.SLASH, location));
            move = 1;
          }
          case '<' -> {
            tokens.add(new Token(TokenType.CMP_LT, location));
            move = 1;
          }
          case '>' -> {
            tokens.add(new Token(TokenType.CMP_GT, location));
            move = 1;
          }
          case '"' -> {
            // TODO: Escape sequences
            int first = i++;
            while (code.charAt(i) != '"') {
              if (code.charAt(i) == '\n') {
                throw CompilerException.builder()
                    .formatMessage("Unterminated string literal")
                    .setLocation(new Location(fileName, line, first))
                    .build();
              }
              i++;
              column++;
            }
            tokens.add(new Token(TokenType.STRING, code.substring(first + 1, i), location));
            i++;
            column++;
          }
          default ->
              throw CompilerException.builder()
                  .formatMessage("Invalid character: %s", c)
                  .setLocation(location)
                  .build();
        }
        column += move;
        i += move;
      }
    }
    return new TokenStream(tokens);
  }
}
