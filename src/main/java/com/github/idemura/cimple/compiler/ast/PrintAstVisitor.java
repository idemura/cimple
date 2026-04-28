package com.github.idemura.cimple.compiler.ast;

import com.github.idemura.cimple.common.IndentWriter;
import com.github.idemura.cimple.compiler.Type;

public class PrintAstVisitor extends AstVisitor {
  private final IndentWriter output;

  public PrintAstVisitor(IndentWriter output) {
    this.output = output;
  }

  public void print(AstNode node) {
    node.accept(this);
  }

  @Override
  protected Object visit(AstModule node) {
    output.writeLine("MODULE %s".formatted(node.getName()));
    output.indent();
    visitChildren(node);
    output.unindent();
    output.writeLine("END");
    return null;
  }

  @Override
  protected Object visit(AstFunction node) {
    printEntity(
        "FUNCTION",
        node.getName(),
        node.getResultType() == null ? null : node.getResultType().getType());
    output.indent();
    for (var p : node.getParameters()) {
      printEntity("ARG", p.getName(), p.getTypeRef().getType());
    }
    node.getBlock().accept(this);
    output.unindent();
    output.writeLine("END");
    return null;
  }

  @Override
  protected Object visit(AstTypeStruct node) {
    output.writeLine("TYPE STRUCT %s".formatted(node.getName()));
    output.indent();
    visitChildren(node);
    output.unindent();
    output.writeLine("END");
    return null;
  }

  @Override
  protected Object visit(AstTypeAlias node) {
    output.writeLine("TYPE ALIAS %s = %s".formatted(node.getName(), node.getBaseTypeRef()));
    return null;
  }

  @Override
  protected Object visit(AstBlock node) {
    output.writeLine("BLOCK");
    output.indent();
    visitChildren(node);
    output.unindent();
    output.writeLine("END");
    return null;
  }

  @Override
  protected Object visit(AstReturn node) {
    output.writeLine("RETURN");
    output.indent();
    visitChildren(node);
    output.unindent();
    return null;
  }

  @Override
  protected Object visit(AstLiteral node) {
    printEntity("LITERAL", node.getValue(), node.getType());
    return null;
  }

  @Override
  protected Object visit(AstNameRef node) {
    output.writeLine("IDENTIFIER %s".formatted(node.getName()));
    return null;
  }

  @Override
  protected Object visit(AstFunctionApply node) {
    output.writeLine("APPLY %s".formatted(node.getFunctionName()));
    output.indent();
    visitChildren(node);
    output.unindent();
    return null;
  }

  @Override
  protected Object visit(AstVariable node) {
    printEntity(node.getMutable() ? "VAR" : "CONST", node.getName(), node.getTypeRef().getType());
    output.indent();
    if (node.getExpression() != null) {
      node.getExpression().accept(this);
    }
    output.unindent();
    return null;
  }

  @Override
  protected Object visit(AstIf node) {
    var conditions = node.getConditions();
    var thenBlocks = node.getThenBlocks();
    for (var i = 0; i < conditions.size(); i++) {
      output.writeLine("IF");
      output.indent();
      conditions.get(i).accept(this);
      output.unindent();
      output.writeLine("THEN");
      output.indent();
      thenBlocks.get(i).accept(this);
      output.unindent();
    }
    if (node.getElseBlock() != null) {
      output.writeLine("ELSE");
      output.indent();
      node.getElseBlock().accept(this);
      output.unindent();
    }
    output.writeLine("END");
    return null;
  }

  @Override
  protected Object visit(AstFor node) {
    output.writeLine("FOR");
    output.indent();
    if (node.getInit() != null) {
      node.getInit().accept(this);
    } else {
      output.writeLine("NONE");
    }
    if (node.getCondition() != null) {
      node.getCondition().accept(this);
    } else {
      output.writeLine("NONE");
    }
    if (node.getIncrement() != null) {
      node.getIncrement().accept(this);
    } else {
      output.writeLine("NONE");
    }
    node.getBlock().accept(this);
    output.unindent();
    output.writeLine("END");
    return null;
  }

  @Override
  protected Object visit(AstGoto node) {
    output.writeLine("GOTO %s".formatted(node.getLabel()));
    return null;
  }

  @Override
  protected Object visit(AstDefer node) {
    output.writeLine("DEFER");
    output.indent();
    node.getExpression().accept(this);
    output.unindent();
    return null;
  }

  @Override
  protected Object visit(AstExpressionStatement node) {
    output.writeLine("EXPR");
    output.indent();
    node.getExpression().accept(this);
    output.unindent();
    return null;
  }

  private void printEntity(String clazz, Object value, Type type) {
    if (type == null) {
      output.writeLine("%s %s".formatted(clazz, value));
    } else {
      output.writeLine("%s %s %s".formatted(clazz, value, type));
    }
  }
}
