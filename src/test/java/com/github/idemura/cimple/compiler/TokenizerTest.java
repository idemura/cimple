package com.github.idemura.cimple.compiler;

import static com.github.idemura.cimple.compiler.TokenType.*;
import static org.junit.jupiter.api.Assertions.*;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;

class TokenizerTest {
  @Test
  void testSplit() {
    var code =
        """
        function foo() {
          # comment
          var bar = 1;
          var x = true;
          var y = null;
        }
        """;
    var errorConsumer = new InMemoryErrorConsumer();
    var tokenizer = new Tokenizer(errorConsumer);
    tokenizer.split(code, null);
    assertEquals(
        ImmutableList.of(
            new Token(IDENTIFIER, "function", new Location(1, 1)),
            new Token(IDENTIFIER, "foo", new Location(1, 10)),
            new Token(LPAREN, null, new Location(1, 13)),
            new Token(RPAREN, null, new Location(1, 14)),
            new Token(LCURLY, null, new Location(1, 16)),
            new Token(IDENTIFIER, "var", new Location(3, 3)),
            new Token(IDENTIFIER, "bar", new Location(3, 7)),
            new Token(ASSIGN, null, new Location(3, 11)),
            new Token(NUMBER, "1", new Location(3, 13)),
            new Token(SEMICOLON, null, new Location(3, 14)),
            new Token(IDENTIFIER, "var", new Location(4, 3)),
            new Token(IDENTIFIER, "x", new Location(4, 7)),
            new Token(ASSIGN, null, new Location(4, 9)),
            new Token(IDENTIFIER, "true", new Location(4, 11)),
            new Token(SEMICOLON, null, new Location(4, 15)),
            new Token(IDENTIFIER, "var", new Location(5, 3)),
            new Token(IDENTIFIER, "y", new Location(5, 7)),
            new Token(ASSIGN, null, new Location(5, 9)),
            new Token(IDENTIFIER, "null", new Location(5, 11)),
            new Token(SEMICOLON, null, new Location(5, 15)),
            new Token(RCURLY, null, new Location(6, 1))),
        tokenizer.tokenList());
  }

  @Test
  void testCompoundAssignmentTokens() {
    var code =
        """
        a += b;
        a -= b;
        a *= b;
        a /= b;
        a %= b;
        """;
    var errorConsumer = new InMemoryErrorConsumer();
    var tokenizer = new Tokenizer(errorConsumer);
    tokenizer.split(code, null);
    assertEquals(
        ImmutableList.of(
            new Token(IDENTIFIER, "a", new Location(1, 1)),
            new Token(PLUS_ASSIGN, null, new Location(1, 3)),
            new Token(IDENTIFIER, "b", new Location(1, 6)),
            new Token(SEMICOLON, null, new Location(1, 7)),
            new Token(IDENTIFIER, "a", new Location(2, 1)),
            new Token(MINUS_ASSIGN, null, new Location(2, 3)),
            new Token(IDENTIFIER, "b", new Location(2, 6)),
            new Token(SEMICOLON, null, new Location(2, 7)),
            new Token(IDENTIFIER, "a", new Location(3, 1)),
            new Token(STAR_ASSIGN, null, new Location(3, 3)),
            new Token(IDENTIFIER, "b", new Location(3, 6)),
            new Token(SEMICOLON, null, new Location(3, 7)),
            new Token(IDENTIFIER, "a", new Location(4, 1)),
            new Token(SLASH_ASSIGN, null, new Location(4, 3)),
            new Token(IDENTIFIER, "b", new Location(4, 6)),
            new Token(SEMICOLON, null, new Location(4, 7)),
            new Token(IDENTIFIER, "a", new Location(5, 1)),
            new Token(PERCENT_ASSIGN, null, new Location(5, 3)),
            new Token(IDENTIFIER, "b", new Location(5, 6)),
            new Token(SEMICOLON, null, new Location(5, 7))),
        tokenizer.tokenList());
  }

  @Test
  void testComparisonTokens() {
    var code =
        """
        a < b;
        a <= b;
        a > b;
        a >= b;
        a == b;
        a != b;
        """;
    var errorConsumer = new InMemoryErrorConsumer();
    var tokenizer = new Tokenizer(errorConsumer);
    tokenizer.split(code, null);
    assertEquals(
        ImmutableList.of(
            new Token(IDENTIFIER, "a", new Location(1, 1)),
            new Token(CMP_LT, null, new Location(1, 3)),
            new Token(IDENTIFIER, "b", new Location(1, 5)),
            new Token(SEMICOLON, null, new Location(1, 6)),
            new Token(IDENTIFIER, "a", new Location(2, 1)),
            new Token(CMP_LE, null, new Location(2, 3)),
            new Token(IDENTIFIER, "b", new Location(2, 6)),
            new Token(SEMICOLON, null, new Location(2, 7)),
            new Token(IDENTIFIER, "a", new Location(3, 1)),
            new Token(CMP_GT, null, new Location(3, 3)),
            new Token(IDENTIFIER, "b", new Location(3, 5)),
            new Token(SEMICOLON, null, new Location(3, 6)),
            new Token(IDENTIFIER, "a", new Location(4, 1)),
            new Token(CMP_GE, null, new Location(4, 3)),
            new Token(IDENTIFIER, "b", new Location(4, 6)),
            new Token(SEMICOLON, null, new Location(4, 7)),
            new Token(IDENTIFIER, "a", new Location(5, 1)),
            new Token(CMP_EQ, null, new Location(5, 3)),
            new Token(IDENTIFIER, "b", new Location(5, 6)),
            new Token(SEMICOLON, null, new Location(5, 7)),
            new Token(IDENTIFIER, "a", new Location(6, 1)),
            new Token(CMP_NE, null, new Location(6, 3)),
            new Token(IDENTIFIER, "b", new Location(6, 6)),
            new Token(SEMICOLON, null, new Location(6, 7))),
        tokenizer.tokenList());
  }
}
