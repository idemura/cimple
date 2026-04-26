package com.github.idemura.cimple.compiler;

import java.util.Objects;

public class AstLiteral extends AstAbstractExpression {
  private TokenType tokenType;
  private Object value;
  private TypeRef typeRef;

  AstLiteral(TokenType tokenType, Object value) {
    this.tokenType = tokenType;
    this.value = value;
  }

  @Override
  public void accept(AstVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public int hashCode() {
    return tokenType.hashCode();
  }

  @Override
  public boolean equals(Object object) {
    return this == object
        || (object instanceof AstLiteral other
            && tokenType == other.tokenType
            && Objects.equals(value, other.value));
  }

  TokenType getTokenType() {
    return tokenType;
  }

  Object getValue() {
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
