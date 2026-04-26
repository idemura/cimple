package com.github.idemura.cimple.compiler;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

class TokenizerTest {
  @Test
  void testSplit() {
    var code = "function foo() {\n  # comment\n  var bar = 1;\n}\n";
    var tokens = new Tokenizer(null, code).split().tokens();
    assertEquals(
        List.of(
            new Token(TokenType.IDENTIFIER, "function", new Location(1, 1)),
            new Token(TokenType.IDENTIFIER, "foo", new Location(1, 10)),
            new Token(TokenType.LPAREN, null, new Location(1, 13)),
            new Token(TokenType.RPAREN, null, new Location(1, 14)),
            new Token(TokenType.LCURLY, null, new Location(1, 16)),
            new Token(TokenType.IDENTIFIER, "var", new Location(3, 3)),
            new Token(TokenType.IDENTIFIER, "bar", new Location(3, 7)),
            new Token(TokenType.ASSIGN, null, new Location(3, 11)),
            new Token(TokenType.NUMBER, "1", new Location(3, 13)),
            new Token(TokenType.SEMICOLON, null, new Location(3, 14)),
            new Token(TokenType.RCURLY, null, new Location(4, 1))),
        tokens);
    assertEquals(TokenType.FUNCTION, tokens.get(0).keyword());
    assertEquals(TokenType.IDENTIFIER, tokens.get(1).keyword());
    assertEquals(TokenType.VAR, tokens.get(5).keyword());
    assertEquals(TokenType.IDENTIFIER, tokens.get(6).keyword());
  }
}
