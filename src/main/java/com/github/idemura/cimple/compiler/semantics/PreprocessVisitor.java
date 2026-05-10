package com.github.idemura.cimple.compiler.semantics;

import static java.lang.Double.parseDouble;
import static java.lang.Long.parseLong;

import com.github.idemura.cimple.compiler.ErrorConsumer;
import com.github.idemura.cimple.compiler.Location;
import com.github.idemura.cimple.compiler.QualifiedName;
import com.github.idemura.cimple.compiler.ast.AstBoolLiteral;
import com.github.idemura.cimple.compiler.ast.AstBuiltinType;
import com.github.idemura.cimple.compiler.ast.AstCall;
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
import com.github.idemura.cimple.compiler.ast.AstTypeRef;
import com.github.idemura.cimple.compiler.ast.AstUnionType;
import com.github.idemura.cimple.compiler.ast.AstVariable;
import java.util.HashMap;
import java.util.List;

class PreprocessVisitor extends AstExpressionRewriteVisitor {
  private final ReservedWords reservedWords;
  private final ErrorConsumer errorConsumer;

  PreprocessVisitor(List<String> reservedWords, ErrorConsumer errorConsumer) {
    this.reservedWords = new ReservedWords(reservedWords);
    this.errorConsumer = errorConsumer;
  }

  @Override
  protected Object visit(AstModule node) {
    checkName(node.name(), node.location());
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
    checkQualifiedName(node.name(), node.location());
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
                "Receiver function '%s': multiple receiver parameters",
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
            "Receiver function '%s': missing the receiver parameter",
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
              "Free function '%s' cannot have a receiver parameter '%s'",
              functionName,
              parameter.name());
        }
      }
    }
  }

  @Override
  protected Object visit(AstVariable node) {
    checkQualifiedName(node.name(), node.location());
    if (!node.getBit(AstVariable.PARAMETER)
        && node.typeRef() == null
        && node.expression() == null) {
      errorConsumer.errorAt(
          node.location(), "Variable '%s' must have a type or an initializer", node.name());
    }
    return super.visit(node);
  }

  @Override
  protected Object visit(AstTypeRef node) {
    switch (node.name().typeName()) {
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
    checkQualifiedName(node.name(), node.location());
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
    checkQualifiedName(node.name(), node.location());
    var fieldMap = new HashMap<String, AstVariable>();
    for (var field : node.fields()) {
      var existing = fieldMap.putIfAbsent(field.name().entityName(), field);
      if (existing != null) {
        errorConsumer.errorAt(
            field.location(),
            "Duplicate record field '%s'. First defined at %s.",
            field.name().entityName(),
            existing.location());
      }
    }
    return super.visit(node);
  }

  @Override
  protected Object visit(AstUnionType node) {
    checkQualifiedName(node.name(), node.location());
    var variantMap = new HashMap<String, AstUnionType.Variant>();
    for (var variant : node.variants()) {
      checkName(variant.tag(), variant.location());
      var existing = variantMap.putIfAbsent(variant.tag(), variant);
      if (existing != null) {
        errorConsumer.errorAt(
            variant.location(),
            "Duplicate union variant '%s'. First defined at %s.",
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
      errorConsumer.errorAt(node.location(), "Invalid number '%s': %s", value, e.getMessage());
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
        switch (node.name().entityName()) {
          case "true" -> {
            var literal = new AstBoolLiteral(true);
            literal.typeRef(AstTypeRef.ofType(AstBuiltinType.BOOL));
            yield literal;
          }
          case "false" -> {
            var literal = new AstBoolLiteral(false);
            literal.typeRef(AstTypeRef.ofType(AstBuiltinType.BOOL));
            yield literal;
          }
          case "null" -> {
            var literal = new AstNullLiteral();
            literal.typeRef(AstTypeRef.ofType(AstBuiltinType.NULL));
            yield literal;
          }
          default -> node;
        };
    if (newNode != node) {
      newNode.location(node.location());
    } else {
      checkQualifiedName(node.name(), node.location());
    }
    return newNode;
  }

  @Override
  protected Object visit(AstCall node) {
    return super.visit(node);
  }

  private void checkName(String name, Location location) {
    if (reservedWords.isReservedName(name)) {
      errorConsumer.errorAt(location, "Reserved word '%s' cannot be used as a name", name);
    }
  }

  private void checkTypeName(String name, Location location) {
    if (reservedWords.isReservedTypeName(name)) {
      errorConsumer.errorAt(location, "Reserved word '%s' cannot be used as a type name", name);
    }
  }

  private void checkQualifiedName(QualifiedName name, Location location) {
    if (name.moduleName() != null) {
      checkName(name.moduleName(), location);
    }
    if (name.typeName() != null) {
      checkTypeName(name.typeName(), location);
    }
    if (name.entityName() != null) {
      checkName(name.entityName(), location);
    }
  }
}
