package io.lang.cimple.compiler.ast;

import com.google.common.collect.ImmutableList;
import io.lang.cimple.compiler.Identifier;
import io.lang.cimple.compiler.Location;
import java.util.List;
import java.util.Objects;

public final class AstUnionType extends AstType {
  public static final class Variant {
    private String tag;
    private Location location;
    private final AstTypeHolder type = new AstTypeHolder();

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

    public AstType valueType() {
      return type.get();
    }

    public void valueType(AstType type) {
      this.type.set(type);
    }
  }

  private Identifier name;
  private List<Variant> variants;

  @Override
  public void accept(AstVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public void acceptChildren(AstVisitor visitor) {
    for (var variant : variants) {
      acceptSafe(variant.type, visitor);
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

  public List<Variant> variants() {
    return variants;
  }

  public void variants(List<Variant> variants) {
    this.variants = ImmutableList.copyOf(variants);
  }

  public boolean hasPayload() {
    for (var variant : variants) {
      if (variant.valueType() != null) {
        return true;
      }
    }
    return false;
  }
}
