package com.github.idemura.cimple.compiler;

import java.util.List;

public class AstFunctionApply extends AstAbstractExpression {
  private String functionName;
  private List<AstAbstractExpression> args;
  private AstFunction func;

  AstFunctionApply() {}

  @Override
  public void accept(Visitor visitor) {
    visitor.visit(this);
  }

  @Override
  TypeRef getTypeRef() {
    return func.getResultType();
  }

  void setFunctionName(String functionName) {
    this.functionName = functionName;
  }

  String getFunctionName() {
    return functionName;
  }

  void setArgs(List<AstAbstractExpression> args) {
    this.args = args;
  }

  List<AstAbstractExpression> getArgs() {
    return List.copyOf(args);
  }

  List<TypeRef> getArgsTypes() {
    return args.stream().map(AstAbstractExpression::getTypeRef).toList();
  }

  void setFunc(AstFunction func) {
    this.func = func;
  }

  AstFunction getFunc() {
    return func;
  }
}
