package com.github.idemura.cimple.compiler.ast;

import com.github.idemura.cimple.compiler.TypeRef;
import java.util.List;

public class AstFunctionApply extends AstExpression {
  private String functionName;
  private List<AstExpression> args;
  private AstFunction func;

  public AstFunctionApply() {}

  @Override
  public void accept(AstVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public TypeRef getTypeRef() {
    return func.getResultType();
  }

  public void setFunctionName(String functionName) {
    this.functionName = functionName;
  }

  public String getFunctionName() {
    return functionName;
  }

  public void setArgs(List<AstExpression> args) {
    this.args = args;
  }

  public List<AstExpression> getArgs() {
    return List.copyOf(args);
  }

  public List<TypeRef> getArgsTypes() {
    return args.stream().map(AstExpression::getTypeRef).toList();
  }

  public void setFunc(AstFunction func) {
    this.func = func;
  }

  public AstFunction getFunc() {
    return func;
  }
}
