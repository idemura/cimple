package com.github.idemura.cimple.compiler.ast;

import com.google.common.collect.ImmutableList;
import java.util.List;

public final class AstTypeStruct extends AstType {
  private QualifiedName name;
  private List<AstVariable> fields;

  public AstTypeStruct() {}

  @Override
  public Object accept(AstVisitor visitor) {
    return visitor.visit(this);
  }

  @Override
  public QualifiedName getName() {
    return name;
  }

  public void setName(QualifiedName name) {
    this.name = name;
  }

  public void setFields(List<AstVariable> fields) {
    this.fields = ImmutableList.copyOf(fields);
  }

  public List<AstVariable> getFields() {
    return ImmutableList.copyOf(fields);
  }
}
