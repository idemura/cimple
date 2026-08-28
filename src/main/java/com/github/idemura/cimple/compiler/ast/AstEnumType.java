package com.github.idemura.cimple.compiler.ast;

import com.github.idemura.cimple.compiler.Identifier;
import com.github.idemura.cimple.compiler.Location;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Objects;

public final class AstEnumType extends AstType {
  public static final class Variant {
    private String tag;
    private Location location;
    private AstExpressionHolder valueExpression;
    private Long value;

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

    public AstExpression valueExpression() {
      return valueExpression == null ? null : valueExpression.get();
    }

    public void valueExpression(AstExpression valueExpression) {
      this.valueExpression = AstExpressionHolder.of(valueExpression);
    }

    public Long value() {
      return value;
    }

    public void value(long value) {
      this.value = value;
    }
  }

  private Identifier name;
  private final AstTypeHolder baseType = new AstTypeHolder();
  private List<Variant> variants = ImmutableList.of();

  @Override
  public void accept(AstVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public void acceptChildren(AstVisitor visitor) {
    acceptSafe(baseType, visitor);
    for (var variant : variants) {
      acceptSafe(variant.valueExpression, visitor);
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

  public AstType baseType() {
    return baseType.get();
  }

  public void baseType(AstType baseType) {
    this.baseType.set(baseType);
  }

  public List<Variant> variants() {
    return variants;
  }

  public void variants(List<Variant> variants) {
    this.variants = ImmutableList.copyOf(variants);
  }
}
