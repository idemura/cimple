package com.github.idemura.cimple.compiler.semantics;

import static java.lang.Double.parseDouble;
import static java.lang.Long.parseLong;

import com.github.idemura.cimple.compiler.ast.AstBoolLiteral;
import com.github.idemura.cimple.compiler.ast.AstLiteral;
import com.github.idemura.cimple.compiler.ast.AstModule;
import com.github.idemura.cimple.compiler.ast.AstName;
import com.github.idemura.cimple.compiler.ast.AstNullLiteral;
import com.github.idemura.cimple.compiler.ast.AstNumberLiteral;
import com.github.idemura.cimple.compiler.ast.AstRewriteExpressionVisitor;
import com.github.idemura.cimple.compiler.ast.AstStringLiteral;
import com.github.idemura.cimple.compiler.ast.AstTypeBuiltin;
import com.github.idemura.cimple.compiler.ast.TypeRef;
import com.github.idemura.cimple.compiler.common.ErrorConsumer;

/// Does the following steps:
///   * Replaces "true", "false", "null" with literals.
///   * Checks identifiers for reserved words.
///   * Transforms numeric literals into typed numeric literals.
class PreprocessRewriteVisitor extends AstRewriteExpressionVisitor {
  private final ErrorConsumer errorConsumer;

  PreprocessRewriteVisitor(ErrorConsumer errorConsumer) {
    this.errorConsumer = errorConsumer;
  }

  @Override
  protected Object visit(AstModule node) {
    return super.visit(node);
  }

  @Override
  protected Object visit(AstLiteral node) {
    if (node.getType() == null) {
      if (node instanceof AstNumberLiteral) {
        AstNumberLiteral number;
        var value = (String) node.value();
        try {
          if (value.contains(".")) {
            number = new AstNumberLiteral(parseDouble(value));
            number.setType(TypeRef.of(AstTypeBuiltin.FLOAT64));
          } else {
            number = new AstNumberLiteral(parseLong(value));
            number.setType(TypeRef.of(AstTypeBuiltin.INT64));
          }
          number.setLocation(node.getLocation());
        } catch (NumberFormatException e) {
          // TODO: Add an error
          // throw CompilerException.builder()
          //     .formatMessage("Invalid numeric literal: %s", token.value())
          //     .setLocation(token.location())
          //     .build();
        }
      } else if (node instanceof AstStringLiteral) {
        node.setType(TypeRef.of(AstTypeBuiltin.STRING));
      }
    }
    return node;
  }

  @Override
  protected Object visit(AstName node) {
    var newNode =
        switch (node.getName().name()) {
          case "true" -> new AstBoolLiteral(true);
          case "false" -> new AstBoolLiteral(false);
          case "null" -> new AstNullLiteral();
          default -> node;
        };
    if (newNode != node) {
      newNode.setLocation(node.getLocation());
    }
    return newNode;
  }

  static boolean isReservedIdentifier(String identifier) {
    return false;
  }
}
