package com.github.idemura.cimple.compiler.parser;

public enum TokenType {
  IDENTIFIER("<identifier>"),
  NUMBER("<number>"),
  STRING("<string>"),
  CHAR("<char>"),
  LPAREN("("),
  RPAREN(")"),
  LBRACKET("["),
  RBRACKET("]"),
  LCURLY("{"),
  RCURLY("}"),
  SEMICOLON(";"),
  COLON(":"),
  TILDE("~"),
  COMMA(","),
  PERIOD("."),
  ASSIGN("="),
  PLUS("+"),
  PLUS_ASSIGN("+="),
  MINUS("-"),
  MINUS_ASSIGN("-="),
  STAR("*"),
  STAR_ASSIGN("*="),
  SLASH("/"),
  SLASH_ASSIGN("/="),
  PERCENT("%"),
  PERCENT_ASSIGN("%="),
  CMP_EQ("=="),
  CMP_NE("!="),
  CMP_LT("<"),
  CMP_GT(">"),
  CMP_LE("<="),
  CMP_GE(">=");

  private final String symbolName;

  TokenType(String symbolName) {
    this.symbolName = symbolName;
  }

  public String symbolName() {
    return symbolName;
  }
}
