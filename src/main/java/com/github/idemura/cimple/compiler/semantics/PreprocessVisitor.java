package com.github.idemura.cimple.compiler.semantics;

import static java.lang.Double.parseDouble;
import static java.lang.Long.parseLong;

import com.github.idemura.cimple.compiler.ErrorConsumer;
import com.github.idemura.cimple.compiler.Location;
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
            errorEntityCollision(function, existing);
          }
        }
        case AstVariable variable -> {
          variable.setBit(AstVariable.GLOBAL);
          variable.setName(variable.getName().withModuleName(moduleName));
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
    for (var parameter : node.getParameters()) {
      parameter.setBit(AstVariable.PARAMETER);
    }
    checkReceiverParameter(node);
    // Default return type if missing.
    if (node.getResultType() == null) {
      node.setResultType(AstTypeRef.ofType(AstBuiltinType.VOID));
    }
    return super.visit(node);
  }

  private void checkReceiverParameter(AstFunctionHeader header) {
    // Receiver functions must have exactly one receiver parameter: the single parameter with no
    // explicit type. Free functions must not have any untyped parameters.
    var parameters = header.getParameters();
    if (header.getReceiverType() != null) {
      var receiverIndex = -1;
      var invalid = false;
      for (int i = 0; i < parameters.size(); i++) {
        if (parameters.get(i).getType() == null) {
          if (receiverIndex >= 0) {
            errorConsumer.errorAt(
                header.getLocation(),
                "Receiver function %s: multiple receiver parameters.",
                header.getName());
            invalid = true;
            break;
          }
          receiverIndex = i;
        }
      }
      if (!invalid && receiverIndex < 0) {
        errorConsumer.errorAt(
            header.getLocation(),
            "Receiver function %s: missing the receiver parameter.",
            header.getName());
      } else {
        header.setReceiverIndex(receiverIndex);
        parameters.get(receiverIndex).setType(header.getReceiverType());
      }
    } else {
      for (var parameter : parameters) {
        if (parameter.getType() == null) {
          errorConsumer.errorAt(
              parameter.getLocation(),
              "Free function %s cannot have a receiver parameter %s.",
              header.getName(),
              parameter.getName());
        }
      }
    }
  }

  @Override
  protected Object visit(AstFunction node) {
    checkName(node.getName().name(), node.getLocation());
    return super.visit(node);
  }

  @Override
  protected Object visit(AstVariable node) {
    checkName(node.getName().name(), node.getLocation());
    if (!node.getBit(AstVariable.PARAMETER)
        && node.getType() == null
        && node.getExpression() == null) {
      errorConsumer.errorAt(
          node.getLocation(),
          "Variable %s must have a type or an initializer.",
          node.getName());
    }
    return super.visit(node);
  }

  @Override
  protected Object visit(AstTypeRef node) {
    switch (node.getName().name()) {
      case "int":
        node.setName(AstBuiltinType.INT64.getName());
        node.setType(AstBuiltinType.INT64);
        break;
      case "float":
        node.setName(AstBuiltinType.FLOAT64.getName());
        node.setType(AstBuiltinType.FLOAT64);
        break;
      default:
        break;
    }
    return super.visit(node);
  }

  @Override
  protected Object visit(AstFunctionType node) {
    checkName(node.getName().name(), node.getLocation());
    return super.visit(node);
  }

  @Override
  protected Object visit(AstLocal node) {
    node.getVariable().setBit(AstVariable.LOCAL);
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
  protected Object visit(AstNullLiteral node) {
    node.setType(AstTypeRef.ofType(AstBuiltinType.NULL));
    return node;
  }

  @Override
  protected Object visit(AstBoolLiteral node) {
    node.setType(AstTypeRef.ofType(AstBuiltinType.BOOL));
    return node;
  }

  @Override
  protected Object visit(AstNumberLiteral node) {
    if (node.getType() != null) {
      return node;
    }
    AstNumberLiteral number;
    var value = (String) node.value();
    try {
      if (value.contains(".")) {
        number = new AstNumberLiteral(parseDouble(value));
        number.setType(AstTypeRef.ofType(AstBuiltinType.FLOAT64));
      } else {
        number = new AstNumberLiteral(parseLong(value));
        number.setType(AstTypeRef.ofType(AstBuiltinType.INT64));
      }
      number.setLocation(node.getLocation());
      return number;
    } catch (NumberFormatException e) {
      errorConsumer.errorAt(node.getLocation(), "Invalid number %s: %s", value, e.getMessage());
      return node;
    }
  }

  @Override
  protected Object visit(AstStringLiteral node) {
    node.setType(AstTypeRef.ofType(AstBuiltinType.STRING));
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
        entity.getLocation(),
        "Definition of %s %s has a name collision with %s defined at %s.",
        entityKind(entity),
        entity.getName(),
        entityKind(existing),
        existing.getLocation());
  }
}
