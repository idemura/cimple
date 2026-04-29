package com.github.idemura.cimple.compiler.ast;

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

  private AstBuiltinType(String name) {
    setModuleName("");
    setName(name);
  }

  @Override
  public Object accept(AstVisitor visitor) {
    return visitor.visit(this);
  }
}
