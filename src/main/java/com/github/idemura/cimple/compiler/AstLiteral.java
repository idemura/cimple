package com.github.idemura.cimple.compiler;

public class AstLiteral extends AstExpression {
  private final TokenType tokenType;
  private final String value;
  private TypeRef typeRef;

  AstLiteral(Location location, TokenType tokenType, String value) {
    super(location);
    this.tokenType = tokenType;
    this.value = value;
  }

  @Override
  public void accept(Visitor visitor) {
    visitor.visit(this);
  }

  @Override
  TypeRef getTypeRef() {
    return typeRef;
  }

  String getValue() {
    return value;
  }

  TokenType getTokenType() {
    return tokenType;
  }

  void setTypeRef(TypeRef typeRef) {
    this.typeRef = typeRef;
  }
}
