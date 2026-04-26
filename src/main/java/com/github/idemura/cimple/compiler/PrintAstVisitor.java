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
  protected void visit(AstTypeStruct node) {
    printIndent();
    output.write("TYPE STRUCT %s\n".formatted(node.getName()));
    indent++;
    visitChildren(node);
    indent--;
  }

  @Override
  protected void visit(AstTypeAlias node) {
    printIndent();
    output.write("TYPE ALIAS %s = %s\n".formatted(node.getName(), node.getBaseTypeRef()));
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
    output.write("LITERAL %s %s".formatted(node.getValue(), node.getTypeRef()));
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
      output.write("IF\n");
      indent++;
      conditions.get(i).accept(this);
      indent--;
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
    output.write("FOR\n");
    indent++;
    printIndent();
    output.write("INIT\n");
    if (node.getInit() != null) {
      node.getInit().accept(this);
    }
    indent--;
    printIndent();
    output.write("INIT\n");
    if (node.getCondition() != null) {
      indent++;
      node.getCondition().accept(this);
      indent--;
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
  protected void visit(AstDefer node) {
    printIndent();
    output.write("DEFER\n");
    indent++;
    node.getExpression().accept(this);
    indent--;
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
