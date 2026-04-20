package com.github.idemura.cimple.compiler;

enum TokenType {
  EOF("<eof>"),
  IDENTIFIER("<identifier>"),
  NUMBER("<number>"),
  STRING("<string>"),
  CHAR("<char>"),
  NULL("<null>"),
  TRUE("<true>"),
  FALSE("<false>"),
  LPAREN("("),
  RPAREN(")"),
  LCURLY("{"),
  RCURLY("}"),
  SEMICOLON(";"),
  COLON(":"),
  COMMA(","),
  ASSIGN(""),
  PLUS("+"),
  MINUS("-"),
  ASTERISK("*"),
  SLASH("/"),
  CMP_EQ("=="),
  CMP_NE("!="),
  CMP_LT("<"),
  CMP_GT(">"),
  FUNCTION,
  VARIABLE,
  RETURN,
  IF,
  ELSE,
  WHILE;

  private final String printableName;

  static TokenType ofKeyword(String value) {
    return switch (value) {
      case "function" -> TokenType.FUNCTION;
      case "var" -> TokenType.VARIABLE;
      case "return" -> TokenType.RETURN;
      case "if" -> TokenType.IF;
      case "else" -> TokenType.ELSE;
      case "while" -> TokenType.WHILE;
      default -> null;
    };
  }

  TokenType() {
    this.printableName = null;
  }

  TokenType(String printableName) {
    this.printableName = printableName;
  }

  String printableName() {
    return printableName == null ? name() : printableName;
  }
}
