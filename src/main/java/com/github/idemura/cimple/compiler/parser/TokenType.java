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
  OP_ADD("+"),
  OP_SUB("-"),
  OP_MUL("*"),
  OP_DIV("/"),
  OP_MOD("%"),
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
