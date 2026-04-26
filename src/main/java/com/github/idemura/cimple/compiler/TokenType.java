package com.github.idemura.cimple.compiler;

enum TokenType {
  EOF("EOF"),
  IDENTIFIER("<identifier>"),
  NUMBER("<number>"),
  STRING("<string>"),
  CHAR("<char>"),
  NULL("null"),
  TRUE("true"),
  FALSE("false"),
  LPAREN("("),
  RPAREN(")"),
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
  DO,
  IF,
  ENUM,
  ELIF,
  ELSE,
  FOR,
  FUNCTION,
  GOTO,
  IMPLEMENT,
  IMPORT,
  INTERFACE,
  MATCH,
  MODULE,
  RECORD,
  RETURN,
  TEMPLATE,
  TYPE,
  VAR,
  WHILE;

  private final String printableName;

  static TokenType ofKeyword(String value) {
    return switch (value) {
      case "alias" -> TokenType.ALIAS;
      case "case" -> TokenType.CASE;
      case "class" -> TokenType.CLASS;
      case "const" -> TokenType.CONST;
      case "do" -> TokenType.DO;
      case "else" -> TokenType.ELSE;
      case "elif" -> TokenType.ELIF;
      case "enum" -> TokenType.ENUM;
      case "for" -> TokenType.FOR;
      case "function" -> TokenType.FUNCTION;
      case "goto" -> TokenType.GOTO;
      case "if" -> TokenType.IF;
      case "implement" -> TokenType.IMPLEMENT;
      case "import" -> TokenType.IMPORT;
      case "interface" -> TokenType.INTERFACE;
      case "match" -> TokenType.MATCH;
      case "module" -> TokenType.MODULE;
      case "return" -> TokenType.RETURN;
      case "record" -> TokenType.RECORD;
      case "type" -> TokenType.TYPE;
      case "var" -> TokenType.VAR;
      case "while" -> TokenType.WHILE;
      default -> null;
    };
  }

  TokenType() {
    this.printableName = name();
  }

  TokenType(String printableName) {
    this.printableName = printableName;
  }

  String printableName() {
    return printableName;
  }
}
