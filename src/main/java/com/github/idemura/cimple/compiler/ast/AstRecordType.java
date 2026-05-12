package com.github.idemura.cimple.compiler.ast;

import com.github.idemura.cimple.compiler.Identifier;
import com.google.common.collect.ImmutableList;
import java.util.List;

public final class AstRecordType extends AstType {
  private Identifier name;
  private List<AstVariable> fields;

  public AstRecordType() {}

  @Override
  public void accept(AstVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public Identifier name() {
    return name;
  }

  @Override
  public void name(Identifier name) {
    this.name = name;
  }

  public List<AstVariable> fields() {
    return fields;
  }

  public void fields(List<AstVariable> fields) {
    this.fields = ImmutableList.copyOf(fields);
  }
}
