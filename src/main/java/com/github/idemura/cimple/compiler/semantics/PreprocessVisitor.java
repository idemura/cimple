package com.github.idemura.cimple.compiler.semantics;

import static java.lang.Double.parseDouble;
import static java.lang.Long.parseLong;

import com.github.idemura.cimple.compiler.ErrorConsumer;
import com.github.idemura.cimple.compiler.Identifier;
import com.github.idemura.cimple.compiler.Location;
import com.github.idemura.cimple.compiler.ast.AstAssign;
import com.github.idemura.cimple.compiler.ast.AstBoolLiteral;
import com.github.idemura.cimple.compiler.ast.AstBuiltinType;
import com.github.idemura.cimple.compiler.ast.AstEntityRef;
import com.github.idemura.cimple.compiler.ast.AstExpression;
import com.github.idemura.cimple.compiler.ast.AstExpressionRewriteVisitor;
import com.github.idemura.cimple.compiler.ast.AstExpressionRewriter;
import com.github.idemura.cimple.compiler.ast.AstFunction;
import com.github.idemura.cimple.compiler.ast.AstFunctionHeader;
import com.github.idemura.cimple.compiler.ast.AstFunctionType;
import com.github.idemura.cimple.compiler.ast.AstLocal;
import com.github.idemura.cimple.compiler.ast.AstModule;
import com.github.idemura.cimple.compiler.ast.AstNullLiteral;
import com.github.idemura.cimple.compiler.ast.AstNumberLiteral;
import com.github.idemura.cimple.compiler.ast.AstRecordType;
import com.github.idemura.cimple.compiler.ast.AstStringLiteral;
import com.github.idemura.cimple.compiler.ast.AstStringType;
import com.github.idemura.cimple.compiler.ast.AstTypeRef;
import com.github.idemura.cimple.compiler.ast.AstUnionType;
import com.github.idemura.cimple.compiler.ast.AstVariable;
import java.util.HashMap;
import java.util.List;

class PreprocessVisitor extends AstExpressionRewriteVisitor {
  private final ReservedWords reservedWords;
  private final ErrorConsumer errorConsumer;

  PreprocessVisitor(List<String> reservedWords, ErrorConsumer errorConsumer) {
    this(new ReservedWords(reservedWords), errorConsumer);
  }

  private PreprocessVisitor(ReservedWords reservedWords, ErrorConsumer errorConsumer) {
    super(new ExpressionRewriter(reservedWords, errorConsumer));
    this.reservedWords = reservedWords;
    this.errorConsumer = errorConsumer;
  }

  @Override
  protected void visit(AstModule node) {
    checkName(node.name(), node.location());
    super.visit(node);
  }

  @Override
  protected void visit(AstFunctionHeader node) {
    for (var parameter : node.parameters()) {
      parameter.setBit(AstVariable.PARAMETER);
    }
    // Default the result type to void when it is omitted.
    if (node.resultType() == null) {
      node.resultType(AstBuiltinType.VOID);
    }
    super.visit(node);
  }

  @Override
  protected void visit(AstFunction node) {
    checkQualifiedName(node.name(), node.location());
    checkReceiverParameter(node.name(), node.header());
    super.visit(node);
  }

  private void checkReceiverParameter(Identifier functionName, AstFunctionHeader header) {
    // Receiver functions must have exactly one receiver parameter: the only parameter without an
    // explicit type. Free functions must not have any untyped parameters.
    var parameters = header.parameters();
    if (header.receiverType() != null) {
      var receiverIndex = -1;
      var invalid = false;
      for (int i = 0; i < parameters.size(); i++) {
        if (parameters.get(i).type() == null) {
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
        parameters.get(receiverIndex).type(header.receiverType());
      }
    } else {
      for (var parameter : parameters) {
        if (parameter.type() == null) {
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
  protected void visit(AstVariable node) {
    checkQualifiedName(node.name(), node.location());
    if (!node.getBit(AstVariable.PARAMETER) && node.type() == null && node.expression() == null) {
      errorConsumer.errorAt(
          node.location(), "Variable '%s' must have a type or an initializer", node.name());
    }
    super.visit(node);
  }

  @Override
  protected void visit(AstTypeRef node) {
    switch (node.name().typeName()) {
      case "int":
        node.name(AstBuiltinType.INT64.name());
        break;
      case "float":
        node.name(AstBuiltinType.FLOAT64.name());
        break;
      default:
        break;
    }
    super.visit(node);
  }

  @Override
  protected void visit(AstFunctionType node) {
    checkQualifiedName(node.name(), node.location());
    checkReceiverParameter(node.name(), node.header());
    super.visit(node);
  }

  @Override
  protected void visit(AstLocal node) {
    node.variable().setBit(AstVariable.LOCAL);
    super.visit(node);
  }

  @Override
  protected void visit(AstRecordType node) {
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
    super.visit(node);
  }

  @Override
  protected void visit(AstUnionType node) {
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
    super.visit(node);
  }

  private static class ExpressionRewriter extends AstExpressionRewriter {
    private final ReservedWords reservedWords;
    private final ErrorConsumer errorConsumer;

    ExpressionRewriter(ReservedWords reservedWords, ErrorConsumer errorConsumer) {
      this.reservedWords = reservedWords;
      this.errorConsumer = errorConsumer;
    }

    @Override
    public AstExpression rewrite(AstAssign node) {
      if (node != root()) {
        errorConsumer.errorAt(
            node.location(), "Assignment is only allowed at the root of an expression");
      }
      return node;
    }

    @Override
    public AstExpression rewrite(AstNullLiteral node) {
      node.type(AstBuiltinType.NULL);
      return node;
    }

    @Override
    public AstExpression rewrite(AstBoolLiteral node) {
      node.type(AstBuiltinType.BOOL);
      return node;
    }

    @Override
    public AstExpression rewrite(AstNumberLiteral node) {
      if (node.type() != null) {
        return node;
      }
      AstNumberLiteral number;
      var value = (String) node.value();
      try {
        if (value.contains(".")) {
          number = new AstNumberLiteral(parseDouble(value));
          number.type(AstBuiltinType.FLOAT64);
        } else {
          number = new AstNumberLiteral(parseLong(value));
          number.type(AstBuiltinType.INT64);
        }
        number.location(node.location());
        return number;
      } catch (NumberFormatException e) {
        errorConsumer.errorAt(node.location(), "Invalid number '%s': %s", value, e.getMessage());
        return node;
      }
    }

    @Override
    public AstExpression rewrite(AstStringLiteral node) {
      node.type(AstStringType.STRING);
      return node;
    }

    @Override
    public AstExpression rewrite(AstEntityRef node) {
      var newNode =
          switch (node.name().entityName()) {
            case "true" -> {
              var literal = new AstBoolLiteral(true);
              literal.type(AstBuiltinType.BOOL);
              yield literal;
            }
            case "false" -> {
              var literal = new AstBoolLiteral(false);
              literal.type(AstBuiltinType.BOOL);
              yield literal;
            }
            case "null" -> {
              var literal = new AstNullLiteral();
              literal.type(AstBuiltinType.NULL);
              yield literal;
            }
            default -> node;
          };
      if (newNode != node) {
        newNode.location(node.location());
      } else {
        checkQualifiedName(reservedWords, errorConsumer, node.name(), node.location());
      }
      return newNode;
    }
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

  private void checkQualifiedName(Identifier name, Location location) {
    checkQualifiedName(reservedWords, errorConsumer, name, location);
  }

  private static void checkQualifiedName(
      ReservedWords reservedWords,
      ErrorConsumer errorConsumer,
      Identifier name,
      Location location) {
    checkName(reservedWords, errorConsumer, name.moduleName(), location);
    checkTypeName(reservedWords, errorConsumer, name.typeName(), location);
    checkName(reservedWords, errorConsumer, name.entityName(), location);
  }

  private static void checkName(
      ReservedWords reservedWords, ErrorConsumer errorConsumer, String name, Location location) {
    if (name != null && reservedWords.isReservedName(name)) {
      errorConsumer.errorAt(location, "Reserved word '%s' cannot be used as a name", name);
    }
  }

  private static void checkTypeName(
      ReservedWords reservedWords, ErrorConsumer errorConsumer, String name, Location location) {
    if (name != null && reservedWords.isReservedTypeName(name)) {
      errorConsumer.errorAt(location, "Reserved word '%s' cannot be used as a type name", name);
    }
  }
}
