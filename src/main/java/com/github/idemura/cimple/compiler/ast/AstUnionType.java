package com.github.idemura.cimple.compiler.ast;

import com.github.idemura.cimple.compiler.Location;
import com.github.idemura.cimple.compiler.QualifiedName;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Objects;

public final class AstUnionType extends AstType {
  public static final class Variant {
    private String tag;
    private Location location;
    private AstTypeRef valueType;

    @Override
    public int hashCode() {
      return tag.hashCode();
    }

    @Override
    public boolean equals(Object object) {
      return this == object || (object instanceof Variant other && Objects.equals(tag, other.tag));
    }

    public Location location() {
      return location;
    }

    public void location(Location location) {
      this.location = location;
    }

    public String tag() {
      return tag;
    }

    public void tag(String tag) {
      this.tag = tag;
    }

    public AstTypeRef valueType() {
      return valueType;
    }

    public void valueType(AstTypeRef valueType) {
      this.valueType = valueType;
    }
  }

  private QualifiedName name;
  private List<Variant> variants;

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

  public List<Variant> variants() {
    return variants;
  }

  public void variants(List<Variant> variants) {
    this.variants = ImmutableList.copyOf(variants);
  }
}
