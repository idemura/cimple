package com.github.idemura.cimple.compiler;

public class AstLiteral extends AstExpression {
  private TokenType tokenType;
  private String value;
  private TypeRef typeRef;

  AstLiteral(TokenType tokenType, String value) {
    this.tokenType = tokenType;
    this.value = value;
  }

  @Override
  public void accept(Visitor visitor) {
    visitor.visit(this);
  }

  TokenType getTokenType() {
    return tokenType;
  }

  String getValue() {
    return value;
  }

  @Override
  TypeRef getTypeRef() {
    return typeRef;
  }

  void setTypeRef(TypeRef typeRef) {
    this.typeRef = typeRef;
  }
}
