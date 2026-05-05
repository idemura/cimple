package com.github.idemura.cimple.compiler.ast;

import com.github.idemura.cimple.compiler.Location;
import java.util.Objects;

public final class UnionVariant {
  private Location location;
  private String tag;
  private AstTypeRef valueType;

  @Override
  public int hashCode() {
    return tag.hashCode();
  }

  @Override
  public boolean equals(Object object) {
    return this == object
        || (object instanceof UnionVariant other && Objects.equals(tag, other.tag));
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

  public void setTag(String name) {
    this.tag = tag;
  }

  public AstTypeRef getValueType() {
    return valueType;
  }

  public void setValueType(AstTypeRef valueType) {
    this.valueType = valueType;
  }
}
