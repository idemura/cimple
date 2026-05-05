package com.github.idemura.cimple.compiler.parser;

import static com.github.idemura.cimple.compiler.parser.Keyword.*;
import static com.github.idemura.cimple.compiler.parser.TokenType.*;
import static org.junit.jupiter.api.Assertions.*;

import com.github.idemura.cimple.compiler.CompilerException;
import org.junit.jupiter.api.Test;

class TokenStreamTest {
  @Test
  void testTakeAndDone() {
    var s = new TokenStream();
    s.add(new Token(IDENTIFIER, "fn"));
    s.add(new Token(LPAREN));
    s.add(new Token(NUMBER, "12"));
    s.add(new Token(RPAREN));

    assertFalse(s.done());
    assertEquals("fn", s.current().value());
    assertEquals("fn", s.take(IDENTIFIER).value());
    assertThrows(CompilerException.class, () -> s.take(NUMBER));
    assertThrows(CompilerException.class, () -> s.take(STRING));
    assertFalse(s.done());
    assertEquals(LPAREN, s.take().type());
    assertEquals("12", s.current().value());
    {
      var t = s.take();
      assertEquals(NUMBER, t.type());
      assertEquals("12", t.value());
    }
    assertFalse(s.done());
    assertEquals(RPAREN, s.take().type());
    assertTrue(s.done());
  }

  @Test
  void testTakeIf() {
    var s = new TokenStream();
    s.add(new Token(IDENTIFIER, "fn"));
    s.add(new Token(LPAREN));
    s.add(new Token(NUMBER, "12"));
    s.add(new Token(RPAREN));

    assertFalse(s.takeIf(NUMBER));
    assertFalse(s.takeIf(STRING));
    assertTrue(s.takeIf(IDENTIFIER));
    assertTrue(s.takeIf(LPAREN));
    assertTrue(s.takeIf(NUMBER));
    assertTrue(s.takeIf(RPAREN));
    assertTrue(s.done());
  }
}
