package com.github.idemura.cimple.compiler.tokens;

public enum TokenType {
  EOF("EOF"),
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
  CMP_GT(">"),
  ALIAS,
  CASE,
  CLASS,
  CONST,
  DEFER,
  IF,
  UNION,
  ELSE,
  FOR,
  FUNCTION,
  GOTO,
  IMPLEMENT,
  IMPORT,
  INTERFACE,
  MATCH,
  MODULE,
  RETURN,
  RECORD,
  TEMPLATE,
  TYPE,
  VAR;

  private final String symbolName;

  public static TokenType ofKeyword(String value) {
    return switch (value) {
      case "alias" -> ALIAS;
      case "case" -> CASE;
      case "class" -> CLASS;
      case "const" -> CONST;
      case "defer" -> DEFER;
      case "else" -> ELSE;
      case "union" -> UNION;
      case "for" -> FOR;
      case "function" -> FUNCTION;
      case "goto" -> GOTO;
      case "if" -> IF;
      case "implement" -> IMPLEMENT;
      case "import" -> IMPORT;
      case "interface" -> INTERFACE;
      case "match" -> MATCH;
      case "module" -> MODULE;
      case "return" -> RETURN;
      case "record" -> RECORD;
      case "type" -> TYPE;
      case "var" -> VAR;
      default -> null;
    };
  }

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
