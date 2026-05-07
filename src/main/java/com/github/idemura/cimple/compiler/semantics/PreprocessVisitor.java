package com.github.idemura.cimple.compiler.semantics;

import static java.lang.Double.parseDouble;
import static java.lang.Long.parseLong;

import com.github.idemura.cimple.compiler.ErrorConsumer;
import com.github.idemura.cimple.compiler.Location;
import com.github.idemura.cimple.compiler.ast.AstBoolLiteral;
import com.github.idemura.cimple.compiler.ast.AstBuiltinType;
import com.github.idemura.cimple.compiler.ast.AstCall;
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
import java.util.HashMap;
import java.util.List;

/// Does the following steps:
///   * Replaces "true", "false", "null" with literals.
///   * Checks identifiers for reserved words.
///   * Transforms numeric literals into typed numeric literals.
class PreprocessVisitor extends AstRewriteExpressionVisitor {
  private final NameMap nameMap;
  private final ReservedWords reservedWords;
  private final ErrorConsumer errorConsumer;
  private String moduleName;

  PreprocessVisitor(NameMap nameMap, List<String> reservedWords, ErrorConsumer errorConsumer) {
    this.nameMap = nameMap;
    this.reservedWords = new ReservedWords(reservedWords);
    this.errorConsumer = errorConsumer;
  }

  @Override
  protected Object visit(AstModule node) {
    checkName(node.getName(), node.getLocation());
    moduleName = node.getName();
    for (var definition : node.definitions()) {
      switch (definition) {
        case AstType type -> {
          type.setName(type.getName().withModuleName(moduleName));
          var existing = nameMap.addType(type);
          if (existing != null) {
            errorConsumer.errorAt(
                type.getLocation(),
                "Duplicate type: %s. Defined at %s.",
                type.getName(),
                existing.getLocation());
          }
        }
        case AstFunction function -> {
          var header = function.getHeader();
          header.setName(header.getName().withModuleName(moduleName));
          var existing = nameMap.addFunction(function);
          if (existing != null) {
            errorConsumer.errorAt(
                header.getLocation(),
                "Duplicate function: %s. Defined at %s.",
                function.getName(),
                existing.getHeader().getLocation());
          }
        }
        case AstVariable variable -> {
          variable.setName(variable.getName().withModuleName(moduleName));
          var existing = nameMap.addVariable(variable);
          if (existing != null) {
            errorConsumer.errorAt(
                variable.getLocation(),
                "Duplicate variable: %s. Defined at %s.",
                variable.getName(),
                existing.getLocation());
          }
        }
        default ->
            throw new IllegalArgumentException(
                "Unsupported module definition: %s".formatted(definition));
      }
    }
    return super.visit(node);
  }

  @Override
  protected Object visit(AstCall node) {
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
    var fieldMap = new HashMap<String, AstVariable>();
    for (var field : node.getFields()) {
      var existing = fieldMap.putIfAbsent(field.getName().name(), field);
      if (existing != null) {
        errorConsumer.errorAt(
            field.getLocation(),
            "Duplicate record field: %s. Defined at %s.",
            field.getName().name(),
            existing.getLocation());
      }
    }
    return super.visit(node);
  }

  @Override
  protected Object visit(AstUnionType node) {
    checkTypeName(node.getName().name(), node.getLocation());
    var variantMap = new HashMap<String, AstUnionType.Variant>();
    for (var variant : node.getVariants()) {
      checkName(variant.getTag(), variant.getLocation());
      var existing = variantMap.putIfAbsent(variant.getTag(), variant);
      if (existing != null) {
        errorConsumer.errorAt(
            variant.getLocation(),
            "Duplicate union variant: %s. Defined at %s.",
            variant.getTag(),
            existing.getLocation());
      }
    }
    return super.visit(node);
  }

  @Override
  protected Object visit(AstLiteral node) {
    if (node.getType() != null) {
      return node;
    }
    return switch (node) {
      case AstNumberLiteral ignored -> {
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
          yield number;
        } catch (NumberFormatException e) {
          errorConsumer.errorAt(node.getLocation(), "Invalid number %s: %s", value, e.getMessage());
          yield node;
        }
      }
      case AstStringLiteral ignored -> {
        node.setType(AstTypeRef.of(AstBuiltinType.STRING));
        yield node;
      }
      case AstBoolLiteral ignored -> {
        node.setType(AstTypeRef.of(AstBuiltinType.BOOL));
        yield node;
      }
      case AstNullLiteral ignored -> {
        node.setType(AstTypeRef.of(AstBuiltinType.NULL));
        yield node;
      }
    };
  }

  @Override
  protected Object visit(AstTypeRef node) {
    AstType type = AstBuiltinType.find(node.getName().name());
    if (type == null) {
      type = nameMap.lookupType(node.getName().name());
    }
    if (type != null) {
      node.setName(type.getName());
      node.setType(type);
    } else {
      errorConsumer.errorAt(node.getLocation(), "Undefined type: %s", node.getName());
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
      var variable = nameMap.lookupVariable(node.getName().name());
      if (variable != null) {
        node.setName(variable.getName());
        node.setEntity(variable);
      }
    }
    return newNode;
  }

  private void checkName(String name, Location location) {
    if (reservedWords.isReservedName(name)) {
      errorConsumer.errorAt(location, "Reserved word cannot be used as a name: %s", name);
    }
  }

  private void checkTypeName(String name, Location location) {
    if (reservedWords.isReservedTypeName(name)) {
      errorConsumer.errorAt(location, "Reserved type name cannot be used as a type name: %s", name);
    }
  }
}
