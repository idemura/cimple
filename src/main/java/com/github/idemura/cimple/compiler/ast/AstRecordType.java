package com.github.idemura.cimple.compiler.ast;

import com.github.idemura.cimple.compiler.QualifiedName;
import com.google.common.collect.ImmutableList;
import java.util.List;

public final class AstRecordType extends AstType {
  private QualifiedName name;
  private List<AstVariable> fields;

  public AstRecordType() {}

  @Override
  public Object accept(AstVisitor visitor) {
    return visitor.visit(this);
  }

  @Override
  public QualifiedName name() {
    return name;
  }

  @Override
  public void name(QualifiedName name) {
    this.name = name;
  }

  public List<AstVariable> fields() {
    return fields;
  }

  public void fields(List<AstVariable> fields) {
    this.fields = ImmutableList.copyOf(fields);
  }
}
