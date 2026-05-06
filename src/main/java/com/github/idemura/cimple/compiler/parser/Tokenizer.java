package com.github.idemura.cimple.compiler.parser;

import static com.github.idemura.cimple.compiler.parser.TokenType.*;
import static java.lang.Character.isDigit;
import static java.lang.Character.isJavaIdentifierPart;
import static java.lang.Character.isJavaIdentifierStart;
import static java.lang.Character.isWhitespace;

import com.github.idemura.cimple.compiler.ErrorConsumer;
import com.github.idemura.cimple.compiler.Location;
import java.util.ArrayList;
import java.util.List;

// Transforms input code into a token stream.
public class Tokenizer {
  private static final class SplitContext {
    private final String code;
    private final String fileName;
    private int index;
    private int line = 1;
    private int column = 1;

    private SplitContext(String code, String fileName) {
      this.code = code;
      this.fileName = fileName;
    }
  }

  private final ErrorConsumer errorConsumer;
  private final List<Token> tokens = new ArrayList<>();
  private int pos;

  public Tokenizer(ErrorConsumer errorConsumer) {
    this.errorConsumer = errorConsumer;
  }

  @Override
  public String toString() {
    if (pos >= tokens.size()) {
      return "Tokenizer: EOF";
    } else {
      return "Tokenizer: pos=%d/%d '%s' location %s"
          .formatted(pos, tokens.size(), tokens.get(pos), tokens.get(pos).location());
    }
  }

  public void split(String code, String fileName) {
    var context = new SplitContext(code, fileName);
    final int n = context.code.length();
    if (n > 0 && context.code.charAt(n - 1) != '\n') {
      throw errorConsumer.fatal("File must end with new line");
    }
    while (context.index < n) {
      char c = context.code.charAt(context.index);
      if (isWhitespace(c)) {
        skipWhitespace(context);
      } else if (isJavaIdentifierStart(c)) {
        tokens.add(takeIdentifier(context));
      } else if (isDigit(c)) {
        tokens.add(takeNumber(context));
      } else {
        switch (c) {
          case '#' -> skipComment(context);
          case '(' -> tokens.add(take1CharToken(context, LPAREN));
          case ')' -> tokens.add(take1CharToken(context, RPAREN));
          case '[' -> tokens.add(take1CharToken(context, LBRACKET));
          case ']' -> tokens.add(take1CharToken(context, RBRACKET));
          case '{' -> tokens.add(take1CharToken(context, LCURLY));
          case '}' -> tokens.add(take1CharToken(context, RCURLY));
          case ':' -> tokens.add(take1CharToken(context, COLON));
          case ',' -> tokens.add(take1CharToken(context, COMMA));
          case '.' -> tokens.add(take1CharToken(context, PERIOD));
          case '=' -> tokens.add(take1CharToken(context, ASSIGN));
          case ';' -> tokens.add(take1CharToken(context, SEMICOLON));
          case '+' -> tokens.add(take1CharToken(context, PLUS));
          case '-' -> tokens.add(take1CharToken(context, MINUS));
          case '*' -> tokens.add(take1CharToken(context, ASTERISK));
          case '/' -> tokens.add(take1CharToken(context, SLASH));
          case '<' -> tokens.add(take1CharToken(context, CMP_GT));
          case '>' -> tokens.add(take1CharToken(context, CMP_LT));
          case '"' -> tokens.add(takeString(context));
          default ->
              throw errorConsumer.fatalAt(currentLocation(context), "Invalid character: %s", c);
        }
      }
    }
  }

  public List<Token> tokenList() {
    return List.copyOf(tokens);
  }

  // public void add(Token token) {
  //   tokens.add(token);
  // }

  public boolean done() {
    return pos == tokens.size();
  }

  public void step() {
    checkPosition();
    pos++;
  }

  public Token take() {
    checkPosition();
    return tokens.get(pos++);
  }

  public boolean takeIf(TokenType type) {
    checkPosition();
    var current = tokens.get(pos);
    if (current.type() != type) {
      return false;
    }
    pos++;
    return true;
  }

  Token current() {
    checkPosition();
    return tokens.get(pos);
  }

  Token next() {
    if (pos + 1 < tokens.size()) {
      return tokens.get(pos + 1);
    } else {
      return null;
    }
  }

  private void skipWhitespace(SplitContext context) {
    while (context.index < context.code.length()
        && isWhitespace(context.code.charAt(context.index))) {
      next(context);
    }
  }

  private void skipComment(SplitContext context) {
    while (context.index < context.code.length() && context.code.charAt(context.index) != '\n') {
      next(context);
    }
  }

  private Token takeIdentifier(SplitContext context) {
    var location = currentLocation(context);
    int first = context.index;
    while (context.index < context.code.length()) {
      if (!isJavaIdentifierPart(context.code.charAt(context.index))) {
        break;
      }
      context.column++;
      context.index++;
    }
    return new Token(IDENTIFIER, context.code.substring(first, context.index), location);
  }

  private Token takeNumber(SplitContext context) {
    var location = currentLocation(context);
    int first = context.index;
    while (context.index < context.code.length()) {
      if (!isDigit(context.code.charAt(context.index))) {
        break;
      }
      context.column++;
      context.index++;
    }
    return new Token(NUMBER, context.code.substring(first, context.index), location);
  }

  private Token takeString(SplitContext context) {
    // TODO: Escape sequences
    var location = currentLocation(context);
    int first = context.index++;
    while (context.code.charAt(context.index) != '"') {
      if (context.code.charAt(context.index) == '\n') {
        throw errorConsumer.fatalAt(location, "Unterminated string literal");
      }
      next(context);
    }
    next(context);
    return new Token(STRING, context.code.substring(first + 1, context.index - 1), location);
  }

  private Token take1CharToken(SplitContext context, TokenType tokenType) {
    var location = currentLocation(context);
    next(context);
    return new Token(tokenType, null, location);
  }

  private Location currentLocation(SplitContext context) {
    return new Location(context.fileName, context.line, context.column);
  }

  private void next(SplitContext context) {
    if (context.code.charAt(context.index) == '\n') {
      context.column = 1;
      context.line++;
    } else {
      context.column++;
    }
    context.index++;
  }

  private void checkPosition() {
    if (pos == tokens.size()) {
      throw new IllegalStateException("Reached token stream end");
    }
  }
}
