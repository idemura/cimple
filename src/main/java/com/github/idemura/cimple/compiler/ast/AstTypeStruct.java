package com.github.idemura.cimple.compiler.ast;

import java.util.ArrayList;
import java.util.List;

public class AstTypeStruct extends AstType {
  private List<AstVariable> fields = new ArrayList<>();

  public AstTypeStruct() {}

  @Override
  public void accept(AstVisitor visitor) {
    visitor.visit(this);
  }

  public void addField(AstVariable field) {
    fields.add(field);
  }

  public List<AstVariable> getFields() {
    return List.copyOf(fields);
  }
}
