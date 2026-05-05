package com.github.idemura.cimple.compiler.semantics;

import static java.lang.Double.parseDouble;
import static java.lang.Long.parseLong;

import com.github.idemura.cimple.compiler.ErrorConsumer;
import com.github.idemura.cimple.compiler.Location;
import com.github.idemura.cimple.compiler.ast.AstBoolLiteral;
import com.github.idemura.cimple.compiler.ast.AstBuiltinType;
import com.github.idemura.cimple.compiler.ast.AstEntityRef;
import com.github.idemura.cimple.compiler.ast.AstFunction;
import com.github.idemura.cimple.compiler.ast.AstFunctionType;
import com.github.idemura.cimple.compiler.ast.AstLiteral;
import com.github.idemura.cimple.compiler.ast.AstModule;
import com.github.idemura.cimple.compiler.ast.AstNullLiteral;
import com.github.idemura.cimple.compiler.ast.AstNumberLiteral;
import com.github.idemura.cimple.compiler.ast.AstRecordType;
import com.github.idemura.cimple.compiler.ast.AstRewriteExpressionVisitor;
import com.github.idemura.cimple.compiler.ast.AstStringLiteral;
import com.github.idemura.cimple.compiler.ast.AstType;
import com.github.idemura.cimple.compiler.ast.AstTypeRef;
import com.github.idemura.cimple.compiler.ast.AstUnionType;
import com.github.idemura.cimple.compiler.ast.AstVariable;
import java.util.List;

/// Does the following steps:
///   * Replaces "true", "false", "null" with literals.
///   * Checks identifiers for reserved words.
///   * Transforms numeric literals into typed numeric literals.
class PreprocessVisitor extends AstRewriteExpressionVisitor {
  private final NameMap nameMap;
  private final ReservedWords reservedWords;
  private final ErrorConsumer errorConsumer;

  PreprocessVisitor(NameMap nameMap, List<String> reservedWords, ErrorConsumer errorConsumer) {
    this.nameMap = nameMap;
    this.reservedWords = new ReservedWords(reservedWords);
    this.errorConsumer = errorConsumer;
  }

  @Override
  protected Object visit(AstModule node) {
    checkName(node.getName(), node.getLocation());
    var moduleName = node.getName();
    for (var definition : node.definitions()) {
      switch (definition) {
        case AstType type -> {
          type.setName(type.getName().withModuleName(moduleName));
          nameMap.addType(type);
        }
        case AstFunction function -> {
          var header = function.getHeader();
          header.setName(header.getName().withModuleName(moduleName));
          nameMap.addEntity(function);
        }
        case AstVariable variable -> {
          variable.setName(variable.getName().withModuleName(moduleName));
          nameMap.addEntity(variable);
        }
        default ->
            throw new IllegalArgumentException(
                "Unsupported module definition: %s".formatted(definition));
      }
    }
    return super.visit(node);
  }

  @Override
  protected Object visit(AstFunction node) {
    checkName(node.getName().name(), node.getLocation());
    return super.visit(node);
  }

  @Override
  protected Object visit(AstVariable node) {
    checkName(node.getName().name(), node.getLocation());
    return super.visit(node);
  }

  @Override
  protected Object visit(AstFunctionType node) {
    checkName(node.getName().name(), node.getLocation());
    return super.visit(node);
  }

  @Override
  protected Object visit(AstRecordType node) {
    checkTypeName(node.getName().name(), node.getLocation());
    return super.visit(node);
  }

  @Override
  protected Object visit(AstUnionType node) {
    checkTypeName(node.getName().name(), node.getLocation());
    for (var variant : node.getVariants()) {
      checkName(variant.getTag(), variant.getLocation());
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
            number.setType(AstTypeRef.of(AstBuiltinType.FLOAT64));
          } else {
            number = new AstNumberLiteral(parseLong(value));
            number.setType(AstTypeRef.of(AstBuiltinType.INT64));
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
        node.setType(AstTypeRef.of(AstBuiltinType.STRING));
      }
    }
    return node;
  }

  @Override
  protected Object visit(AstEntityRef node) {
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
      checkName(node.getName().name(), node.getLocation());
    }
    return newNode;
  }

  private void checkName(String name, Location location) {
    if (reservedWords.isReservedName(name)) {
      errorConsumer.error(location, "Reserved word cannot be used as a name: %s", name);
    }
  }

  private void checkTypeName(String name, Location location) {
    if (reservedWords.isReservedTypeName(name)) {
      errorConsumer.error(location, "Reserved type name cannot be used as a type name: %s", name);
    }
  }
}
