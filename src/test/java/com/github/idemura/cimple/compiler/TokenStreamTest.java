package com.github.idemura.cimple.compiler;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class TokenStreamTest {
  @Test
  void testTakeAndDone() {
    var s = new TokenStream();
    s.add(new Token(TokenType.IDENTIFIER, "fn"));
    s.add(new Token(TokenType.LPAREN));
    s.add(new Token(TokenType.NUMBER, "12"));
    s.add(new Token(TokenType.RPAREN));

    assertFalse(s.done());
    assertEquals("fn", s.current().value());
    assertEquals("fn", s.take(TokenType.IDENTIFIER).value());
    assertThrows(CompilerException.class, () -> s.take(TokenType.NUMBER));
    assertThrows(CompilerException.class, () -> s.take(TokenType.STRING));
    assertFalse(s.done());
    assertEquals(TokenType.LPAREN, s.take().type());
    assertEquals("12", s.current().value());
    {
      var t = s.take();
      assertEquals(TokenType.NUMBER, t.type());
      assertEquals("12", t.value());
    }
    assertFalse(s.done());
    assertEquals(TokenType.RPAREN, s.take().type());
    assertTrue(s.done());
  }

  @Test
  void testTakeIf() {
    var s = new TokenStream();
    s.add(new Token(TokenType.IDENTIFIER, "fn"));
    s.add(new Token(TokenType.LPAREN));
    s.add(new Token(TokenType.NUMBER, "12"));
    s.add(new Token(TokenType.RPAREN));

    assertNull(s.takeIf(TokenType.NUMBER));
    assertNull(s.takeIf(TokenType.STRING));
    assertEquals("fn", s.takeIf(TokenType.IDENTIFIER).value());
    assertEquals(TokenType.LPAREN, s.takeIf(TokenType.LPAREN).type());
    assertEquals("12", s.takeIf(TokenType.NUMBER).value());
    assertEquals(TokenType.RPAREN, s.takeIf(TokenType.RPAREN).type());
    assertTrue(s.done());
  }

  @Test
  void testTakeKeyword() {
    var s = new TokenStream();
    s.add(new Token(TokenType.IDENTIFIER, "function"));
    s.add(new Token(TokenType.IDENTIFIER, "foo"));
    s.add(new Token(TokenType.LPAREN, null));
    s.add(new Token(TokenType.RPAREN, null));
    s.add(new Token(TokenType.LCURLY, null));
    s.add(new Token(TokenType.IDENTIFIER, "var"));
    s.add(new Token(TokenType.IDENTIFIER, "bar"));
    s.add(new Token(TokenType.ASSIGN, null));
    s.add(new Token(TokenType.NUMBER, "12"));
    s.add(new Token(TokenType.SEMICOLON, null));
    s.add(new Token(TokenType.RCURLY, null));
    {
      var t = s.takeKeyword(TokenType.FUNCTION);
      assertEquals(TokenType.FUNCTION, t.type());
      assertEquals("function", t.value());
    }
    assertThrows(CompilerException.class, () -> s.takeKeyword(TokenType.FUNCTION));
    assertEquals("foo", s.take().value());
    assertEquals(TokenType.LPAREN, s.take().type());
    assertEquals(TokenType.RPAREN, s.take().type());
    assertEquals(TokenType.LCURLY, s.take().type());
    {
      var t = s.takeKeyword(TokenType.VAR);
      assertEquals(TokenType.VAR, t.type());
      assertEquals("var", t.value());
    }
    {
      var t = s.take();
      assertEquals(TokenType.IDENTIFIER, t.type());
      assertEquals("bar", t.value());
    }
    assertEquals(TokenType.ASSIGN, s.take().type());
    assertEquals(TokenType.NUMBER, s.take().type());
    assertEquals(TokenType.SEMICOLON, s.take().type());
    assertEquals(TokenType.RCURLY, s.take().type());
  }
}
