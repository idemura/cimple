package com.github.idemura.cimple.compiler;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

class TokenizerTest {
  @Test
  void testSplit() {
    var tokenizer = new Tokenizer(null, "function foo()\n{\n# skip it\n  var x = 1;\n}\n");
    assertEquals(
        List.of(
            new Token(TokenType.FUNCTION, new Location(1, 1)),
            new Token(TokenType.IDENTIFIER, "foo", new Location(1, 10)),
            new Token(TokenType.LPAREN, new Location(1, 13)),
            new Token(TokenType.RPAREN, new Location(1, 14)),
            new Token(TokenType.LCURLY, new Location(2, 1)),
            new Token(TokenType.VARIABLE, new Location(4, 3)),
            new Token(TokenType.IDENTIFIER, "x", new Location(4, 7)),
            new Token(TokenType.ASSIGN, new Location(4, 9)),
            new Token(TokenType.NUMBER, "1", new Location(4, 11)),
            new Token(TokenType.SEMICOLON, new Location(4, 12)),
            new Token(TokenType.RCURLY, new Location(5, 1))),
        tokenizer.split().tokens());
  }
}
