package com.github.idemura.cimple.compiler.tokens;

import static com.github.idemura.cimple.compiler.common.Keyword.*;
import static org.junit.jupiter.api.Assertions.*;

import com.github.idemura.cimple.compiler.CompilerException;
import org.junit.jupiter.api.Test;

class TokenTest {
  @Test
  void testKeyword() {
    assertEquals(FUNCTION, new Token(TokenType.IDENTIFIER, "function").keyword());
    assertEquals(VAR, new Token(TokenType.IDENTIFIER, "var").keyword());
    assertEquals(FOR, new Token(TokenType.IDENTIFIER, "for").keyword());
    assertEquals(IF, new Token(TokenType.IDENTIFIER, "if").keyword());
    assertEquals(TYPE, new Token(TokenType.IDENTIFIER, "type").keyword());
    assertThrows(CompilerException.class, () -> new Token(TokenType.IDENTIFIER, "main").keyword());
    assertThrows(CompilerException.class, () -> new Token(TokenType.NUMBER, "100").keyword());
    assertThrows(CompilerException.class, () -> new Token(TokenType.STRING, "var").keyword());
  }
}
