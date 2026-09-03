package io.lang.cimple.compiler;

public final class AstVariableBuilder {
  private Identifier name;
  private AstType type;
  private AstExpression expression;
  private long flags;

  public AstVariableBuilder() {}

  public void name(Identifier name) {
    this.name = name;
  }

  public void type(AstType type) {
    this.type = type;
  }

  public void expression(AstExpression expression) {
    this.expression = expression;
  }

  public void flag(long flag) {
    flags |= flag;
  }

  public AstVariable build() {
    var variable = new AstVariable(name, type, expression);
    variable.flags(flags);
    return variable;
  }
}
