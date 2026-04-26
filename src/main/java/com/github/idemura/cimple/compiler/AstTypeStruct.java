package com.github.idemura.cimple.compiler;

import java.util.ArrayList;
import java.util.List;

public class AstTypeStruct extends AstAbstractType {
  private List<AstVariable> fields = new ArrayList<>();

  AstTypeStruct() {}

  @Override
  public void accept(AstVisitor visitor) {
    visitor.visit(this);
  }

  void addField(AstVariable field) {
    fields.add(field);
  }

  List<AstVariable> getFields() {
    return List.copyOf(fields);
  }
}
