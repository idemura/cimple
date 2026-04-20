package com.github.idemura.cimple.compiler;

import java.util.List;

public class AstFunctionApply extends AstExpression {
  private final String functionName;
  private final List<AstExpression> args;
  private AstFunction func;

  AstFunctionApply(Location location, String functionName, List<AstExpression> args) {
    super(location);
    this.functionName = functionName;
    this.args = args;
  }

  @Override
  public void accept(Visitor visitor) {
    visitor.visit(this);
  }

  @Override
  TypeRef getTypeRef() {
    return func.getResultType();
  }

  String getFunctionName() {
    return functionName;
  }

  List<AstExpression> getArgs() {
    return List.copyOf(args);
  }

  List<TypeRef> getArgsTypes() {
    return args.stream().map(AstExpression::getTypeRef).toList();
  }

  void setFunc(AstFunction func) {
    this.func = func;
  }

  AstFunction getFunc() {
    return func;
  }
}
