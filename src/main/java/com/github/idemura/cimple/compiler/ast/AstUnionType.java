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

    public Location getLocation() {
      return location;
    }

    public void setLocation(Location location) {
      this.location = location;
    }

    public String getTag() {
      return tag;
    }

    public void setTag(String tag) {
      this.tag = tag;
    }

    public AstTypeRef getValueType() {
      return valueType;
    }

    public void setValueType(AstTypeRef valueType) {
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
  public QualifiedName getName() {
    return name;
  }

  @Override
  public void setName(QualifiedName name) {
    this.name = name;
  }

  public List<Variant> getVariants() {
    return variants;
  }

  public void setVariants(List<Variant> variants) {
    this.variants = ImmutableList.copyOf(variants);
  }
}
