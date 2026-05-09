package com.github.idemura.cimple.compiler.semantics;

import static java.lang.Double.parseDouble;
import static java.lang.Long.parseLong;

import com.github.idemura.cimple.compiler.ErrorConsumer;
import com.github.idemura.cimple.compiler.Location;
import com.github.idemura.cimple.compiler.QualifiedName;
import com.github.idemura.cimple.compiler.ast.AstBoolLiteral;
import com.github.idemura.cimple.compiler.ast.AstBuiltinType;
import com.github.idemura.cimple.compiler.ast.AstCall;
import com.github.idemura.cimple.compiler.ast.AstEntity;
import com.github.idemura.cimple.compiler.ast.AstEntityRef;
import com.github.idemura.cimple.compiler.ast.AstExpressionRewriteVisitor;
import com.github.idemura.cimple.compiler.ast.AstFunction;
import com.github.idemura.cimple.compiler.ast.AstFunctionHeader;
import com.github.idemura.cimple.compiler.ast.AstFunctionType;
import com.github.idemura.cimple.compiler.ast.AstLocal;
import com.github.idemura.cimple.compiler.ast.AstModule;
import com.github.idemura.cimple.compiler.ast.AstNullLiteral;
import com.github.idemura.cimple.compiler.ast.AstNumberLiteral;
import com.github.idemura.cimple.compiler.ast.AstRecordType;
import com.github.idemura.cimple.compiler.ast.AstStringLiteral;
import com.github.idemura.cimple.compiler.ast.AstType;
import com.github.idemura.cimple.compiler.ast.AstTypeRef;
import com.github.idemura.cimple.compiler.ast.AstUnionType;
import com.github.idemura.cimple.compiler.ast.AstVariable;
import java.util.HashMap;
import java.util.List;

class PreprocessVisitor extends AstExpressionRewriteVisitor {
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
    checkName(node.name(), node.location());
    moduleName = node.name();
    for (var definition : node.definitions()) {
      switch (definition) {
        case AstType type -> {
          type.name(type.name().withModuleName(moduleName));
          var existing = nameMap.addType(type);
          if (existing != null) {
            errorConsumer.errorAt(
                type.location(),
                "Duplicate type: %s. Defined at %s.",
                type.name(),
                existing.location());
          }
        }
        case AstFunction function -> {
          var header = function.header();
          var receiverType = header.receiverType();
          if (receiverType != null) {
            receiverType.name(receiverType.name().withModuleName(moduleName));
          }
          function.name(function.name().withModuleName(moduleName));
          var existing = nameMap.addFunction(function);
          if (existing != null) {
            errorEntityCollision(function, existing);
          }
        }
        case AstVariable variable -> {
          variable.setBit(AstVariable.GLOBAL);
          variable.name(variable.name().withModuleName(moduleName));
          var existing = nameMap.addVariable(variable);
          if (existing != null) {
            errorEntityCollision(variable, existing);
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
  protected Object visit(AstFunctionHeader node) {
    for (var parameter : node.parameters()) {
      parameter.setBit(AstVariable.PARAMETER);
    }
    // Default the result type to void when it is omitted.
    if (node.resultType() == null) {
      node.resultType(AstTypeRef.ofType(AstBuiltinType.VOID));
    }
    return super.visit(node);
  }

  @Override
  protected Object visit(AstFunction node) {
    checkName(node.name().name(), node.location());
    checkReceiverParameter(node.name(), node.header());
    return super.visit(node);
  }

  private void checkReceiverParameter(QualifiedName functionName, AstFunctionHeader header) {
    // Receiver functions must have exactly one receiver parameter: the only parameter without an
    // explicit type. Free functions must not have any untyped parameters.
    var parameters = header.parameters();
    if (header.receiverType() != null) {
      var receiverIndex = -1;
      var invalid = false;
      for (int i = 0; i < parameters.size(); i++) {
        if (parameters.get(i).typeRef() == null) {
          if (receiverIndex >= 0) {
            errorConsumer.errorAt(
                header.location(),
                "Receiver function %s: multiple receiver parameters.",
                functionName);
            invalid = true;
            break;
          }
          receiverIndex = i;
        }
      }
      if (!invalid && receiverIndex < 0) {
        errorConsumer.errorAt(
            header.location(),
            "Receiver function %s: missing the receiver parameter.",
            functionName);
      } else {
        header.receiverIndex(receiverIndex);
        parameters.get(receiverIndex).typeRef(header.receiverType());
      }
    } else {
      for (var parameter : parameters) {
        if (parameter.typeRef() == null) {
          errorConsumer.errorAt(
              parameter.location(),
              "Free function %s cannot have a receiver parameter %s.",
              functionName,
              parameter.name());
        }
      }
    }
  }

  @Override
  protected Object visit(AstVariable node) {
    checkName(node.name().name(), node.location());
    if (!node.getBit(AstVariable.PARAMETER)
        && node.typeRef() == null
        && node.expression() == null) {
      errorConsumer.errorAt(
          node.location(), "Variable %s must have a type or an initializer.", node.name());
    }
    return super.visit(node);
  }

  @Override
  protected Object visit(AstTypeRef node) {
    switch (node.name().name()) {
      case "int":
        node.name(AstBuiltinType.INT64.name());
        node.type(AstBuiltinType.INT64);
        break;
      case "float":
        node.name(AstBuiltinType.FLOAT64.name());
        node.type(AstBuiltinType.FLOAT64);
        break;
      default:
        break;
    }
    return super.visit(node);
  }

  @Override
  protected Object visit(AstFunctionType node) {
    checkName(node.name().name(), node.location());
    checkReceiverParameter(node.name(), node.header());
    return super.visit(node);
  }

  @Override
  protected Object visit(AstLocal node) {
    node.variable().setBit(AstVariable.LOCAL);
    return super.visit(node);
  }

  @Override
  protected Object visit(AstRecordType node) {
    checkTypeName(node.name().name(), node.location());
    var fieldMap = new HashMap<String, AstVariable>();
    for (var field : node.fields()) {
      var existing = fieldMap.putIfAbsent(field.name().name(), field);
      if (existing != null) {
        errorConsumer.errorAt(
            field.location(),
            "Duplicate record field: %s. Defined at %s.",
            field.name().name(),
            existing.location());
      }
    }
    return super.visit(node);
  }

  @Override
  protected Object visit(AstUnionType node) {
    checkTypeName(node.name().name(), node.location());
    var variantMap = new HashMap<String, AstUnionType.Variant>();
    for (var variant : node.variants()) {
      checkName(variant.tag(), variant.location());
      var existing = variantMap.putIfAbsent(variant.tag(), variant);
      if (existing != null) {
        errorConsumer.errorAt(
            variant.location(),
            "Duplicate union variant: %s. Defined at %s.",
            variant.tag(),
            existing.location());
      }
    }
    return super.visit(node);
  }

  @Override
  protected Object visit(AstNullLiteral node) {
    node.typeRef(AstTypeRef.ofType(AstBuiltinType.NULL));
    return node;
  }

  @Override
  protected Object visit(AstBoolLiteral node) {
    node.typeRef(AstTypeRef.ofType(AstBuiltinType.BOOL));
    return node;
  }

  @Override
  protected Object visit(AstNumberLiteral node) {
    if (node.typeRef() != null) {
      return node;
    }
    AstNumberLiteral number;
    var value = (String) node.value();
    try {
      if (value.contains(".")) {
        number = new AstNumberLiteral(parseDouble(value));
        number.typeRef(AstTypeRef.ofType(AstBuiltinType.FLOAT64));
      } else {
        number = new AstNumberLiteral(parseLong(value));
        number.typeRef(AstTypeRef.ofType(AstBuiltinType.INT64));
      }
      number.location(node.location());
      return number;
    } catch (NumberFormatException e) {
      errorConsumer.errorAt(node.location(), "Invalid number %s: %s", value, e.getMessage());
      return node;
    }
  }

  @Override
  protected Object visit(AstStringLiteral node) {
    node.typeRef(AstTypeRef.ofType(AstBuiltinType.STRING));
    return node;
  }

  @Override
  protected Object visit(AstEntityRef node) {
    var newNode =
        switch (node.name().name()) {
          case "true" -> new AstBoolLiteral(true);
          case "false" -> new AstBoolLiteral(false);
          case "null" -> new AstNullLiteral();
          default -> node;
        };
    if (newNode != node) {
      newNode.location(node.location());
    } else {
      checkName(node.name().name(), node.location());
    }
    return newNode;
  }

  @Override
  protected Object visit(AstCall node) {
    return super.visit(node);
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

  private static String entityKind(AstEntity entity) {
    return switch (entity) {
      case AstFunction ignored -> "function";
      case AstVariable ignored -> "variable";
    };
  }

  private void errorEntityCollision(AstEntity entity, AstEntity existing) {
    errorConsumer.errorAt(
        entity.location(),
        "Definition of %s %s has a name collision with %s defined at %s.",
        entityKind(entity),
        entity.name(),
        entityKind(existing),
        existing.location());
  }
}
