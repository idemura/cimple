package io.lang.cimple.compiler.ast;

import io.lang.cimple.compiler.Identifier;
import com.google.common.collect.ImmutableList;
import java.util.List;

public final class AstStructType extends AstType {
  private Identifier name;
  private List<AstVariable> fields;

  public AstStructType() {}

  @Override
  public void accept(AstVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public void acceptChildren(AstVisitor visitor) {
    for (var field : fields) {
      field.accept(visitor);
    }
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
