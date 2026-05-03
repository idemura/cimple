package com.github.idemura.cimple.compiler.semantics;

import static com.github.idemura.cimple.compiler.semantics.ReservedWords.isReservedTypeName;
import static com.github.idemura.cimple.compiler.semantics.ReservedWords.isReservedWord;
import static java.lang.Double.parseDouble;
import static java.lang.Long.parseLong;

import com.github.idemura.cimple.compiler.Location;
import com.github.idemura.cimple.compiler.ast.AstBoolLiteral;
import com.github.idemura.cimple.compiler.ast.AstFunction;
import com.github.idemura.cimple.compiler.ast.AstLiteral;
import com.github.idemura.cimple.compiler.ast.AstModule;
import com.github.idemura.cimple.compiler.ast.AstName;
import com.github.idemura.cimple.compiler.ast.AstNullLiteral;
import com.github.idemura.cimple.compiler.ast.AstNumberLiteral;
import com.github.idemura.cimple.compiler.ast.AstRewriteExpressionVisitor;
import com.github.idemura.cimple.compiler.ast.AstStringLiteral;
import com.github.idemura.cimple.compiler.ast.AstType;
import com.github.idemura.cimple.compiler.ast.AstTypeAlias;
import com.github.idemura.cimple.compiler.ast.AstTypeBuiltin;
import com.github.idemura.cimple.compiler.ast.AstTypeFunction;
import com.github.idemura.cimple.compiler.ast.AstTypeRecord;
import com.github.idemura.cimple.compiler.ast.AstTypeUnion;
import com.github.idemura.cimple.compiler.ast.AstVariable;
import com.github.idemura.cimple.compiler.ast.QualifiedName;
import com.github.idemura.cimple.compiler.ast.TypeRef;
import com.github.idemura.cimple.compiler.common.ErrorConsumer;

/// Does the following steps:
///   * Replaces "true", "false", "null" with literals.
///   * Checks identifiers for reserved words.
///   * Transforms numeric literals into typed numeric literals.
class PreprocessVisitor extends AstRewriteExpressionVisitor {
  private final ErrorConsumer errorConsumer;

  PreprocessVisitor(ErrorConsumer errorConsumer) {
    this.errorConsumer = errorConsumer;
  }

  @Override
  protected Object visit(AstModule node) {
    checkName(node.getName(), node.getLocation());
    var moduleName = node.getName();
    for (var definition : node.definitions()) {
      if (definition instanceof AstType type) {
        type.getName().setModuleName(moduleName);
      } else if (definition instanceof AstFunction function) {
        function.getHeader().getName().setModuleName(moduleName);
      } else if (definition instanceof AstVariable variable) {
        variable.getName().setModuleName(moduleName);
      } else {
        throw new IllegalArgumentException(
            "Unsupported module definition: %s".formatted(definition));
      }
    }
    return super.visit(node);
  }

  @Override
  protected Object visit(AstFunction node) {
    var header = node.getHeader();
    checkName(header.getName(), header.getLocation());
    checkTypeName(header.getBoundTypeName(), header.getLocation());
    return super.visit(node);
  }

  @Override
  protected Object visit(AstVariable node) {
    checkName(node.getName(), node.getLocation());
    return super.visit(node);
  }

  @Override
  protected Object visit(AstTypeAlias node) {
    checkTypeName(node.getName(), node.getLocation());
    return super.visit(node);
  }

  @Override
  protected Object visit(AstTypeFunction node) {
    checkTypeName(node.getName(), node.getLocation());
    return super.visit(node);
  }

  @Override
  protected Object visit(AstTypeRecord node) {
    checkTypeName(node.getName(), node.getLocation());
    return super.visit(node);
  }

  @Override
  protected Object visit(AstTypeUnion node) {
    checkTypeName(node.getName(), node.getLocation());
    for (var variant : node.getVariants()) {
      checkName(variant.getName(), variant.getLocation());
    }
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
    } else {
      checkName(node.getName(), node.getLocation());
    }
    return newNode;
  }

  private void checkName(String name, Location location) {
    if (name != null && isReservedWord(name)) {
      errorConsumer.error(location, "Reserved word cannot be used as a name: %s", name);
    }
  }

  private void checkName(QualifiedName name, Location location) {
    checkName(name.name(), location);
  }

  private void checkTypeName(QualifiedName name, Location location) {
    if (name != null && isReservedTypeName(name.name())) {
      errorConsumer.error(
          location, "Reserved type name cannot be used as a type name: %s", name.name());
    }
  }

  private void checkTypeName(String name, Location location) {
    if (name != null && isReservedTypeName(name)) {
      errorConsumer.error(location, "Reserved type name cannot be used as a type name: %s", name);
    }
  }
}
