package com.github.idemura.cimple.compiler.ast;

public final class AstTypeBuiltin extends AstType {
  public static final AstTypeBuiltin VOID = new AstTypeBuiltin("void");
  public static final AstTypeBuiltin NULL = new AstTypeBuiltin("null");
  public static final AstTypeBuiltin BOOL = new AstTypeBuiltin("bool");
  public static final AstTypeBuiltin BYTE = new AstTypeBuiltin("byte");
  public static final AstTypeBuiltin INT8 = new AstTypeBuiltin("int8");
  public static final AstTypeBuiltin INT16 = new AstTypeBuiltin("int16");
  public static final AstTypeBuiltin INT32 = new AstTypeBuiltin("int32");
  public static final AstTypeBuiltin INT64 = new AstTypeBuiltin("int64");
  public static final AstTypeBuiltin FLOAT32 = new AstTypeBuiltin("float32");
  public static final AstTypeBuiltin FLOAT64 = new AstTypeBuiltin("float64");
  public static final AstTypeBuiltin CHAR = new AstTypeBuiltin("char");
  public static final AstTypeBuiltin STRING = new AstTypeBuiltin("string");

  private QualifiedName name;

  private AstTypeBuiltin(String name) {
    this.name = new QualifiedName("builtin", name);
  }

  @Override
  public Object accept(AstVisitor visitor) {
    return visitor.visit(this);
  }

  @Override
  public QualifiedName getName() {
    return name;
  }
}
