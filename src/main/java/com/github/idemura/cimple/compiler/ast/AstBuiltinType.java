package com.github.idemura.cimple.compiler.ast;

import com.github.idemura.cimple.compiler.QualifiedName;

public final class AstBuiltinType extends AstType {
  public static final AstBuiltinType VOID = new AstBuiltinType("void");
  public static final AstBuiltinType NULL = new AstBuiltinType("null");
  public static final AstBuiltinType BOOL = new AstBuiltinType("bool");
  public static final AstBuiltinType BYTE = new AstBuiltinType("byte");
  public static final AstBuiltinType INT8 = new AstBuiltinType("int8");
  public static final AstBuiltinType INT16 = new AstBuiltinType("int16");
  public static final AstBuiltinType INT32 = new AstBuiltinType("int32");
  public static final AstBuiltinType INT64 = new AstBuiltinType("int64");
  public static final AstBuiltinType FLOAT32 = new AstBuiltinType("float32");
  public static final AstBuiltinType FLOAT64 = new AstBuiltinType("float64");
  public static final AstBuiltinType CHAR = new AstBuiltinType("char");
  public static final AstBuiltinType STRING = new AstBuiltinType("string");

  private QualifiedName name;

  private AstBuiltinType(String name) {
    this.name = new QualifiedName("builtin", name);
  }

  public static AstBuiltinType find(String name) {
    return switch (name) {
      case "bool" -> BOOL;
      case "byte" -> BYTE;
      case "char" -> CHAR;
      case "float32" -> FLOAT32;
      case "float64" -> FLOAT64;
      case "int8" -> INT8;
      case "int16" -> INT16;
      case "int32" -> INT32;
      case "int64" -> INT64;
      case "null" -> NULL;
      case "string" -> STRING;
      case "void" -> VOID;
      default -> null;
    };
  }

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
    throw new UnsupportedOperationException();
  }
}
