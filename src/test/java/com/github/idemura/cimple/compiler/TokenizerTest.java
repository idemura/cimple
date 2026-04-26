package com.github.idemura.cimple.compiler;

import static com.github.idemura.cimple.compiler.TokenType.*;
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
            new Token(RCURLY, null, new Location(4, 1))),
        tokens);
    assertEquals(FUNCTION, tokens.get(0).keyword());
    assertEquals(IDENTIFIER, tokens.get(1).keyword());
    assertEquals(VAR, tokens.get(5).keyword());
    assertEquals(IDENTIFIER, tokens.get(6).keyword());
  }
}
