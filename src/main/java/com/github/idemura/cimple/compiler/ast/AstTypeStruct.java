package com.github.idemura.cimple.compiler.ast;

import java.util.ArrayList;
import java.util.List;

public final class AstTypeStruct extends AstType {
  private List<AstVariable> fields = new ArrayList<>();

  public AstTypeStruct() {}

  @Override
  public Object accept(AstVisitor visitor) {
    return visitor.visit(this);
  }

  public void addField(AstVariable field) {
    fields.add(field);
  }

  public List<AstVariable> getFields() {
    return List.copyOf(fields);
  }
}
