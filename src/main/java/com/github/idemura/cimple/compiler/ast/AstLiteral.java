package com.github.idemura.cimple.compiler.ast;

import com.github.idemura.cimple.compiler.TokenType;
import com.github.idemura.cimple.compiler.TypeRef;
import java.util.Objects;

public class AstLiteral extends AstExpression {
  private TokenType tokenType;
  private Object value;
  private TypeRef typeRef;

  public AstLiteral(TokenType tokenType, Object value) {
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

  public TokenType getTokenType() {
    return tokenType;
  }

  public Object getValue() {
    return value;
  }

  @Override
  public TypeRef getTypeRef() {
    return typeRef;
  }

  public void setTypeRef(TypeRef typeRef) {
    this.typeRef = typeRef;
  }
}
