package io.lang.cimple.compiler;

import static io.lang.cimple.compiler.TokenType.ASSIGN;
import static io.lang.cimple.compiler.TokenType.BANG;
import static io.lang.cimple.compiler.TokenType.CMP_EQ;
import static io.lang.cimple.compiler.TokenType.CMP_GE;
import static io.lang.cimple.compiler.TokenType.CMP_GT;
import static io.lang.cimple.compiler.TokenType.CMP_LE;
import static io.lang.cimple.compiler.TokenType.CMP_LT;
import static io.lang.cimple.compiler.TokenType.CMP_NE;
import static io.lang.cimple.compiler.TokenType.IDENTIFIER;
import static io.lang.cimple.compiler.TokenType.LCURLY;
import static io.lang.cimple.compiler.TokenType.LPAREN;
import static io.lang.cimple.compiler.TokenType.MINUS_ASSIGN;
import static io.lang.cimple.compiler.TokenType.NUMBER;
import static io.lang.cimple.compiler.TokenType.PERCENT_ASSIGN;
import static io.lang.cimple.compiler.TokenType.PLUS_ASSIGN;
import static io.lang.cimple.compiler.TokenType.RCURLY;
import static io.lang.cimple.compiler.TokenType.RPAREN;
import static io.lang.cimple.compiler.TokenType.SEMICOLON;
import static io.lang.cimple.compiler.TokenType.SLASH_ASSIGN;
import static io.lang.cimple.compiler.TokenType.STAR_ASSIGN;
import static org.junit.jupiter.api.Assertions.*;
import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import java.util.List;

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
    var errorConsumer = new ErrorConsumer();
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
    var errorConsumer = new ErrorConsumer();
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
  void testCharTokens() {
    var code =
        """
        a < b;
        a <= b;
        a > b;
        a >= b;
        a == b;
        a!;
        a != b;
        """;
    var errorConsumer = new ErrorConsumer();
    var tokenizer = new Tokenizer(errorConsumer);
    tokenizer.split(code, null);
    assertEquals(
        List.of(
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
            new Token(BANG, null, new Location(6, 2)),
            new Token(SEMICOLON, null, new Location(6, 3)),
            new Token(IDENTIFIER, "a", new Location(7, 1)),
            new Token(CMP_NE, null, new Location(7, 3)),
            new Token(IDENTIFIER, "b", new Location(7, 6)),
            new Token(SEMICOLON, null, new Location(7, 7))),
        tokenizer.tokenList());
  }
}
