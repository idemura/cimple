package com.github.idemura.cimple.compiler;

import com.github.idemura.cimple.common.StringOutput;

public class PrintVisitor extends Visitor {
  private final StringOutput output;
  private int indent;

  public PrintVisitor(StringOutput output) {
    this.output = output;
  }

  public void print(VisitorNode node) {
    node.accept(this);
  }

  @Override
  protected void visit(AstModule node) {
    printIndent();
    output.write("MODULE\n");

    indent++;
    visitChildren(node);
    indent--;
  }

  @Override
  protected void visit(AstFunction node) {
    printIndent();
    printEntity("FUNCTION", node.getName(), node.getResultType());

    indent++;
    for (var p : node.getParameters()) {
      printIndent();
      printEntity("PARAM", p.getName(), p.getTypeRef());
    }
    node.getBlock().accept(this);
    indent--;
  }

  @Override
  protected void visit(AstBlock node) {
    printIndent();
    output.write("BLOCK\n");

    indent++;
    visitChildren(node);
    indent--;
  }

  @Override
  protected void visit(AstReturn node) {
    printIndent();
    output.write("RETURN\n");

    indent++;
    visitChildren(node);
    indent--;
  }

  @Override
  protected void visit(AstLiteral node) {
    printIndent();
    printEntity("LITERAL", node.getValue(), node.getTypeRef());
  }

  @Override
  protected void visit(AstFunctionApply node) {
    printIndent();
    output.write("APPLY %s\n".formatted(node.getFunctionName()));
    indent++;
    visitChildren(node);
    indent--;
  }

  @Override
  protected void visit(AstVariable node) {
    printIndent();
    printEntity("VAR", node.getName(), node.getTypeRef());
    indent++;
    if (node.getInit() != null) {
      node.getInit().accept(this);
    }
    indent--;
  }

  @Override
  protected void visit(AstNameRef node) {
    printIndent();
    output.write("IDENTIFIER %s\n".formatted(node.getName()));
  }

  @Override
  protected void visit(AstIf node) {
    printIndent();
    output.write("CONDITION\n");
    indent++;
    node.getCondition().accept(this);
    indent--;

    printIndent();
    output.write("THEN\n");
    indent++;
    node.getThenBlock().accept(this);
    indent--;

    printIndent();
    output.write("ELSE\n");
    indent++;
    node.getElseBlock().accept(this);
    indent--;
  }

  @Override
  protected void visit(AstExpressionStatement node) {
    printIndent();
    output.write("EXPR_STMT\n");
    indent++;
    node.getExpression().accept(this);
    indent--;
  }

  private void printIndent() {
    output.write("  ".repeat(indent));
  }

  private void printEntity(String clazz, String name, TypeRef typeRef) {
    if (typeRef == null) {
      output.write("%s %s\n".formatted(clazz, name));
    } else {
      output.write("%s %s: %s\n".formatted(clazz, name, typeRef));
    }
  }
}
