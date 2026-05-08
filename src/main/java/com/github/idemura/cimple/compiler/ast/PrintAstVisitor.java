package com.github.idemura.cimple.compiler.ast;

import com.github.idemura.cimple.compiler.IndentWriter;

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
    output.writeLine("MODULE %s".formatted(node.name()));
    output.indent();
    visitChildren(node);
    output.unindent();
    output.writeLine("END");
    return null;
  }

  @Override
  protected Object visit(AstFunctionHeader node) {
    return super.visit(node);
  }

  @Override
  protected Object visit(AstFunction node) {
    var header = node.header();
    printEntity("FUNCTION", header.name(), header.resultType());
    output.indent();
    for (var parameter : header.parameters()) {
      printEntity("ARG", parameter.name(), parameter.type());
    }
    node.block().accept(this);
    output.unindent();
    output.writeLine("END");
    return null;
  }

  @Override
  protected Object visit(AstVariable node) {
    printEntity(node.getBit(AstVariable.MUTABLE) ? "VAR" : "CONST", node.name(), node.type());
    output.indent();
    if (node.expression() != null) {
      node.expression().accept(this);
    }
    output.unindent();
    return null;
  }

  @Override
  protected Object visit(AstTypeRef node) {
    return super.visit(node);
  }

  @Override
  protected Object visit(AstBuiltinType node) {
    output.writeLine("TYPE BUILTIN %s".formatted(node.name()));
    return null;
  }

  @Override
  protected Object visit(AstFunctionType node) {
    var header = node.header();
    printEntity("TYPE FUNCTION", node.name(), header.resultType());
    output.indent();
    for (var parameter : header.parameters()) {
      printEntity("ARG", parameter.name(), parameter.type());
    }
    output.unindent();
    return null;
  }

  @Override
  protected Object visit(AstRecordType node) {
    output.writeLine("TYPE RECORD %s".formatted(node.name()));
    output.indent();
    visitChildren(node);
    output.unindent();
    output.writeLine("END");
    return null;
  }

  @Override
  protected Object visit(AstUnionType node) {
    output.writeLine("TYPE UNION %s".formatted(node.name()));
    output.indent();
    for (var variant : node.variants()) {
      if (variant.valueType() == null) {
        output.writeLine("VARIANT %s".formatted(variant.tag()));
      } else {
        output.writeLine("VARIANT %s(%s)".formatted(variant.tag(), variant.valueType()));
      }
    }
    output.unindent();
    output.writeLine("END");
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
  protected Object visit(AstExpressionStatement node) {
    output.writeLine("EXPR");
    output.indent();
    node.expression().accept(this);
    output.unindent();
    return null;
  }

  @Override
  protected Object visit(AstLocal node) {
    return super.visit(node);
  }

  @Override
  protected Object visit(AstIf node) {
    var conditions = node.conditions();
    var thenBlocks = node.thenBlocks();
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
    if (node.elseBlock() != null) {
      output.writeLine("ELSE");
      output.indent();
      node.elseBlock().accept(this);
      output.unindent();
    }
    output.writeLine("END");
    return null;
  }

  @Override
  protected Object visit(AstFor node) {
    output.writeLine("FOR");
    output.indent();
    if (node.init() != null) {
      node.init().accept(this);
    } else {
      output.writeLine("NONE");
    }
    if (node.condition() != null) {
      node.condition().accept(this);
    } else {
      output.writeLine("NONE");
    }
    if (node.increment() != null) {
      node.increment().accept(this);
    } else {
      output.writeLine("NONE");
    }
    node.block().accept(this);
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
  protected Object visit(AstDefer node) {
    output.writeLine("DEFER");
    output.indent();
    node.block().accept(this);
    output.unindent();
    return null;
  }

  @Override
  protected Object visit(AstGoto node) {
    output.writeLine("GOTO %s".formatted(node.label()));
    return null;
  }

  @Override
  protected Object visit(AstNullLiteral node) {
    printLiteral(node);
    return null;
  }

  @Override
  protected Object visit(AstBoolLiteral node) {
    printLiteral(node);
    return null;
  }

  @Override
  protected Object visit(AstNumberLiteral node) {
    printLiteral(node);
    return null;
  }

  @Override
  protected Object visit(AstStringLiteral node) {
    printLiteral(node);
    return null;
  }

  @Override
  protected Object visit(AstEntityRef node) {
    output.writeLine("IDENTIFIER %s".formatted(node.name()));
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
  protected Object visit(AstArrayAccess node) {
    output.writeLine("INDEX");
    output.indent();
    node.array().accept(this);
    node.index().accept(this);
    output.unindent();
    return null;
  }

  @Override
  protected Object visit(AstFieldAccess node) {
    output.writeLine("FIELD %s".formatted(node.fieldName()));
    output.indent();
    node.object().accept(this);
    output.unindent();
    return null;
  }

  @Override
  protected Object visit(AstReceiverLookup node) {
    output.writeLine("BIND %s".formatted(node.functionName()));
    output.indent();
    node.object().accept(this);
    output.unindent();
    return null;
  }

  @Override
  protected Object visit(AstCast node) {
    output.writeLine("CAST %s".formatted(node.type()));
    output.indent();
    node.expression().accept(this);
    output.unindent();
    return null;
  }

  private void printLiteral(AstLiteral node) {
    printEntity("LITERAL", node.value(), node.type());
  }

  private void printEntity(String kind, Object value, AstTypeRef type) {
    if (type == null) {
      output.writeLine("%s %s".formatted(kind, value));
    } else {
      output.writeLine("%s %s %s".formatted(kind, value, type));
    }
  }
}
