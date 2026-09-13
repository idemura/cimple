package io.lang.cimple.compiler;

import static io.lang.cimple.compiler.TokenType.ASSIGN;
import static io.lang.cimple.compiler.TokenType.BANG;
import static io.lang.cimple.compiler.TokenType.CMP_EQ;
import static io.lang.cimple.compiler.TokenType.CMP_GE;
import static io.lang.cimple.compiler.TokenType.CMP_GT;
import static io.lang.cimple.compiler.TokenType.CMP_LE;
import static io.lang.cimple.compiler.TokenType.CMP_LT;
import static io.lang.cimple.compiler.TokenType.CMP_NE;
import static io.lang.cimple.compiler.TokenType.COLON;
import static io.lang.cimple.compiler.TokenType.COMMA;
import static io.lang.cimple.compiler.TokenType.IDENTIFIER;
import static io.lang.cimple.compiler.TokenType.LBRACKET;
import static io.lang.cimple.compiler.TokenType.LCURLY;
import static io.lang.cimple.compiler.TokenType.LPAREN;
import static io.lang.cimple.compiler.TokenType.MINUS;
import static io.lang.cimple.compiler.TokenType.MINUS_ASSIGN;
import static io.lang.cimple.compiler.TokenType.NUMBER;
import static io.lang.cimple.compiler.TokenType.PERCENT;
import static io.lang.cimple.compiler.TokenType.PERCENT_ASSIGN;
import static io.lang.cimple.compiler.TokenType.PERIOD;
import static io.lang.cimple.compiler.TokenType.PLUS;
import static io.lang.cimple.compiler.TokenType.PLUS_ASSIGN;
import static io.lang.cimple.compiler.TokenType.RBRACKET;
import static io.lang.cimple.compiler.TokenType.RCURLY;
import static io.lang.cimple.compiler.TokenType.RPAREN;
import static io.lang.cimple.compiler.TokenType.SEMICOLON;
import static io.lang.cimple.compiler.TokenType.SLASH;
import static io.lang.cimple.compiler.TokenType.SLASH_ASSIGN;
import static io.lang.cimple.compiler.TokenType.STAR;
import static io.lang.cimple.compiler.TokenType.STAR_ASSIGN;
import static io.lang.cimple.compiler.TokenType.STRING;
import static io.lang.cimple.compiler.TokenType.TILDE;
import static java.lang.Character.isDigit;
import static java.lang.Character.isJavaIdentifierPart;
import static java.lang.Character.isJavaIdentifierStart;
import static java.lang.Character.isWhitespace;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;

// Converts source code into a flat token stream with source locations.
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
          case '(' -> tokens.add(takeSingleCharToken(context, LPAREN));
          case ')' -> tokens.add(takeSingleCharToken(context, RPAREN));
          case '[' -> tokens.add(takeSingleCharToken(context, LBRACKET));
          case ']' -> tokens.add(takeSingleCharToken(context, RBRACKET));
          case '{' -> tokens.add(takeSingleCharToken(context, LCURLY));
          case '}' -> tokens.add(takeSingleCharToken(context, RCURLY));
          case ':' -> tokens.add(takeSingleCharToken(context, COLON));
          case '~' -> tokens.add(takeSingleCharToken(context, TILDE));
          case ',' -> tokens.add(takeSingleCharToken(context, COMMA));
          case '.' -> tokens.add(takeSingleCharToken(context, PERIOD));
          case '=' -> tokens.add(takeSuffixToken(context, ASSIGN, CMP_EQ));
          case '!' -> tokens.add(takeSuffixToken(context, BANG, CMP_NE));
          case ';' -> tokens.add(takeSingleCharToken(context, SEMICOLON));
          case '+' -> tokens.add(takeSuffixToken(context, PLUS, PLUS_ASSIGN));
          case '-' -> tokens.add(takeSuffixToken(context, MINUS, MINUS_ASSIGN));
          case '*' -> tokens.add(takeSuffixToken(context, STAR, STAR_ASSIGN));
          case '/' -> tokens.add(takeSuffixToken(context, SLASH, SLASH_ASSIGN));
          case '%' -> tokens.add(takeSuffixToken(context, PERCENT, PERCENT_ASSIGN));
          case '<' -> tokens.add(takeSuffixToken(context, CMP_LT, CMP_LE));
          case '>' -> tokens.add(takeSuffixToken(context, CMP_GT, CMP_GE));
          case '"' -> tokens.add(takeString(context));
          default ->
              throw errorConsumer.fatalAt(currentLocation(context), "Invalid character: %s", c);
        }
      }
    }
  }

  public ImmutableList<Token> tokenList() {
    return ImmutableList.copyOf(tokens);
  }

  public boolean done() {
    return pos == tokens.size();
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

  public Token current() {
    checkPosition();
    return tokens.get(pos);
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
    // TODO: Support escape sequences.
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

  private Token takeSingleCharToken(SplitContext context, TokenType tokenType) {
    var location = currentLocation(context);
    next(context);
    return new Token(tokenType, null, location);
  }

  private Token takeSuffixToken(SplitContext context, TokenType baseToken, TokenType suffixToken) {
    var location = currentLocation(context);
    next(context);
    if (context.index < context.code.length() && context.code.charAt(context.index) == '=') {
      next(context);
      return new Token(suffixToken, null, location);
    }
    return new Token(baseToken, null, location);
  }

  private Token takeRequiredSuffixToken(
      SplitContext context, char base, char suffix, TokenType tokenType) {
    var location = currentLocation(context);
    next(context);
    if (context.index < context.code.length() && context.code.charAt(context.index) == suffix) {
      next(context);
      return new Token(tokenType, null, location);
    }
    throw errorConsumer.fatalAt(location, "Expected '%s' after '%s'", suffix, base);
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
