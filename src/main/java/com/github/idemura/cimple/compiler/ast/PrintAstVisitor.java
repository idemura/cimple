package com.github.idemura.cimple.compiler.ast;

import com.github.idemura.cimple.common.IndentWriter;

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
    var header = node.getHeader();
    printEntity("FUNCTION", header.getName(), header.getResultType());
    output.indent();
    for (var p : header.getParameters()) {
      printEntity("ARG", p.getName(), p.getTypeRef());
    }
    node.getBlock().accept(this);
    output.unindent();
    output.writeLine("END");
    return null;
  }

  @Override
  protected Object visit(AstTypeRecord node) {
    output.writeLine("TYPE RECORD %s".formatted(node.getName()));
    output.indent();
    visitChildren(node);
    output.unindent();
    output.writeLine("END");
    return null;
  }

  @Override
  protected Object visit(AstTypeAlias node) {
    output.writeLine("TYPE OPAQUE %s %s".formatted(node.getName(), node.getBaseTypeRef()));
    return null;
  }

  @Override
  protected Object visit(AstTypeFunction node) {
    var header = node.getHeader();
    printEntity("TYPE FUNCTION", node.getName(), header.getResultType());
    output.indent();
    for (var p : header.getParameters()) {
      printEntity("ARG", p.getName(), p.getTypeRef());
    }
    output.unindent();
    return null;
  }

  @Override
  protected Object visit(AstTypeUnion node) {
    output.writeLine("TYPE UNION %s".formatted(node.getName()));
    output.indent();
    for (var variant : node.getVariants()) {
      if (variant.getValueType() == null) {
        output.writeLine("VARIANT %s".formatted(variant.getName()));
      } else {
        output.writeLine("VARIANT %s(%s)".formatted(variant.getName(), variant.getValueType()));
      }
    }
    output.unindent();
    output.writeLine("END");
    return null;
  }

  @Override
  protected Object visit(AstTypeBuiltin node) {
    output.writeLine("TYPE BUILTIN %s".formatted(node.getName()));
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
    printEntity("LITERAL", node.value(), node.getType());
    return null;
  }

  @Override
  protected Object visit(AstFieldAccess node) {
    output.writeLine("FIELD %s".formatted(node.getFieldName()));
    output.indent();
    node.getObject().accept(this);
    output.unindent();
    return null;
  }

  @Override
  protected Object visit(AstBind node) {
    output.writeLine("BIND %s".formatted(node.getFunctionName()));
    output.indent();
    node.getObject().accept(this);
    output.unindent();
    return null;
  }

  @Override
  protected Object visit(AstArrayAccess node) {
    output.writeLine("INDEX");
    output.indent();
    node.getArray().accept(this);
    node.getIndex().accept(this);
    output.unindent();
    return null;
  }

  @Override
  protected Object visit(AstCast node) {
    output.writeLine("CAST %s".formatted(node.getTypeRef()));
    output.indent();
    node.getExpression().accept(this);
    output.unindent();
    return null;
  }

  @Override
  protected Object visit(AstName node) {
    output.writeLine("IDENTIFIER %s".formatted(node.getName()));
    return null;
  }

  @Override
  protected Object visit(AstCall node) {
    output.writeLine("CALL");
    output.indent();
    visitChildren(node);
    output.unindent();
    return null;
  }

  @Override
  protected Object visit(AstVariable node) {
    printEntity(
        node.getBit(AstVariable.MUTABLE) ? "VAR" : "CONST", node.getName(), node.getTypeRef());
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
    node.getBlock().accept(this);
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

  private void printEntity(String clazz, Object value, TypeRef type) {
    if (type == null) {
      output.writeLine("%s %s".formatted(clazz, value));
    } else {
      output.writeLine("%s %s %s".formatted(clazz, value, type));
    }
  }
}
