package com.github.idemura.cimple.compiler;

import com.github.idemura.cimple.common.StringOutput;

public class PrintAstVisitor extends AstVisitor {
  private final StringOutput output;
  private int indent;

  public PrintAstVisitor(StringOutput output) {
    this.output = output;
  }

  public void print(AstAbstractNode node) {
    node.accept(this);
  }

  @Override
  protected void visit(AstModule node) {
    printIndent();
    output.write("MODULE %s\n".formatted(node.getName()));
    indent++;
    visitChildren(node);
    indent--;
  }

  @Override
  protected void visit(AstFunction node) {
    printIndent();
    printEntity("FUNCTION", node.getName(), node.getResultType());
    for (var p : node.getParameters()) {
      printIndent();
      printEntity("ARG", p.getName(), p.getTypeRef());
    }
    indent++;
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
    output.write("RETURN");
    visitChildren(node);
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
    printEntity(node.getMutable() ? "VAR" : "CONST", node.getName(), node.getTypeRef());
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
    var conditions = node.getConditions();
    var thenBlocks = node.getThenBlocks();
    for (var i = 0; i < conditions.size(); i++) {
      printIndent();
      output.write("IF ");
      conditions.get(i).accept(this);
      output.write("\n");
      printIndent();
      output.write("THEN\n");
      indent++;
      thenBlocks.get(i).accept(this);
      indent--;
    }
    if (node.getElseBlock() != null) {
      printIndent();
      output.write("ELSE\n");
      indent++;
      node.getElseBlock().accept(this);
      indent--;
    }
  }

  @Override
  protected void visit(AstFor node) {
    printIndent();
    output.write("FOR ");
    if (node.getCondition() != null) {
      node.getCondition().accept(this);
    }
    indent++;
    node.getBlock().accept(this);
    indent--;
  }

  @Override
  protected void visit(AstGoto node) {
    printIndent();
    output.write("GOTO %s\n".formatted(node.getLabel()));
  }

  @Override
  protected void visit(AstExpressionStatement node) {
    printIndent();
    output.write("EXPR\n");
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
