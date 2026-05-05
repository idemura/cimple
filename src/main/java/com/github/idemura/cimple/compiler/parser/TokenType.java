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
  COMMA(","),
  PERIOD("."),
  ASSIGN("="),
  PLUS("+"),
  MINUS("-"),
  ASTERISK("*"),
  SLASH("/"),
  CMP_EQ("=="),
  CMP_NE("!="),
  CMP_LT("<"),
  CMP_GT(">");

  private final String symbolName;

  TokenType() {
    this.symbolName = name();
  }

  TokenType(String symbolName) {
    this.symbolName = symbolName;
  }

  public String symbolName() {
    return symbolName;
  }
}
