package io.lang.cimple.compiler;

import static java.lang.Double.parseDouble;
import static java.lang.Long.parseLong;

import java.util.HashMap;

// Runs AST checks and rewrites that do not require name resolution:
//  - Validates identifiers
//  - Marks parameters and locals
//  - Sets missing function result types to void
//  - Checks that variables have either a type or an initializer
//  - Checks duplicate function parameters, struct fields, union variants, and enum variants
//  - Normalizes builtin type aliases (int)
//  - Rejects nested assignments
//  - Types literal nodes
class PreprocessVisitor extends AstExpressionRewriteVisitor {
  private final ErrorConsumer errorConsumer;
  private final NormalizeTypeNameVisitor normalizeTypeNameVisitor = new NormalizeTypeNameVisitor();
  private AstModule module;

  PreprocessVisitor(ErrorConsumer errorConsumer) {
    this.errorConsumer = errorConsumer;
  }

  @Override
  protected void visit(AstModule node) {
    module = node;
    checkIdentifier(node.name());
    super.visit(node);
  }

  @Override
  protected void visit(AstFunction node) {
    checkIdentifier(node.name());
    checkFunctionSignature(node);
    super.visit(node);
  }

  @Override
  protected void visit(AstVariable node) {
    checkIdentifier(node.name());
    if (!node.getBit(AstVariable.PARAMETER) && node.type() == null && node.expression() == null) {
      errorConsumer.errorAt(
          node.location(), "Variable '%s' must have a type or an initializer", node.name());
    }
    super.visit(node);
  }

  @Override
  protected void visit(AstTypeRef node) {
    normalizeTypeNameVisitor.normalize(node);
    super.visit(node);
  }

  @Override
  protected void visit(AstFunctionType node) {
    checkTypeName(node.name());
    checkFunctionSignature(node.function());
    node.function().acceptChildren(this);
  }

  @Override
  protected void visit(AstInterfaceType node) {
    checkTypeName(node.name());
    super.visit(node);
  }

  @Override
  protected void visit(AstStructType node) {
    checkTypeName(node.name());
    var fieldMap = new HashMap<String, AstVariable>();
    for (var field : node.fields()) {
      field.flags(AstVariable.FIELD);
      var existing = fieldMap.putIfAbsent(field.name().entity(), field);
      if (existing != null) {
        errorConsumer.errorAt(
            field.location(),
            "Duplicate struct field '%s'. First defined at %s.",
            field.name().entity(),
            existing.location());
      }
    }
    super.visit(node);
  }

  @Override
  protected void visit(AstUnionType node) {
    checkTypeName(node.name());
    var variantMap = new HashMap<Identifier, AstUnionVariant>();
    for (var variant : node.variants()) {
      var existing = variantMap.putIfAbsent(variant.tag(), variant);
      if (existing != null) {
        errorConsumer.errorAt(
            variant.tag().location(),
            "Duplicate union variant '%s'. First defined at %s.",
            variant.tag(),
            existing.tag().location());
      }
    }
    super.visit(node);
  }

  @Override
  protected void visit(AstUnionVariant node) {
    checkTagName(node.tag());
    super.visit(node);
  }

  @Override
  protected void visit(AstLocal node) {
    node.variable().flags(AstVariable.LOCAL);
    super.visit(node);
  }

  @Override
  protected void visit(AstEnumType node) {
    checkTypeName(node.name());
    var variantMap = new HashMap<String, AstEnumVariant>();
    for (var variant : node.variants()) {
      var existing = variantMap.putIfAbsent(variant.tag().entity(), variant);
      if (existing != null) {
        errorConsumer.errorAt(
            variant.tag().location(),
            "Duplicate enum variant '%s'. First defined at %s.",
            variant.tag().entity(),
            existing.tag().location());
      }
    }
    super.visit(node);
  }

  @Override
  protected void visit(AstEnumVariant node) {
    checkTagName(node.tag());
    super.visit(node);
  }

  private void checkFunctionSignature(AstFunction function) {
    if (function.resultType() == null) {
      function.resultType(AstBuiltinType.VOID);
    }
    var parameterMap = new HashMap<String, AstVariable>();
    for (var parameter : function.parameters()) {
      parameter.flags(AstVariable.PARAMETER);
      if (parameter.type() == null) {
        errorConsumer.errorAt(
            parameter.location(),
            "Function '%s' parameter '%s' must have a type",
            function.name(),
            parameter.name());
      }
      var existing = parameterMap.putIfAbsent(parameter.name().entity(), parameter);
      if (existing != null) {
        errorConsumer.errorAt(
            parameter.location(),
            "Duplicate function parameter '%s'. First defined at %s.",
            parameter.name().entity(),
            existing.location());
      }
    }
  }

  @Override
  public AstExpression rewrite(AstAssign node) {
    if (node != expressionRoot()) {
      errorConsumer.errorAt(
          node.location(), "Assignment is only allowed at the root of an expression");
    }
    return node;
  }

  @Override
  public AstExpression rewrite(AstCompoundAssign node) {
    if (node != expressionRoot()) {
      errorConsumer.errorAt(
          node.location(), "Assignment is only allowed at the root of an expression");
    }
    return node;
  }

  @Override
  public AstExpression rewrite(AstNew node) {
    normalizeTypeNameVisitor.normalize(node.type());
    return node;
  }

  @Override
  public AstExpression rewrite(AstCall node) {
    return super.rewrite(node);
  }

  @Override
  public AstExpression rewrite(AstNullLiteral node) {
    // Type is already assigned on creation
    return node;
  }

  @Override
  public AstExpression rewrite(AstBoolLiteral node) {
    // Type is already assigned on creation
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
        number = new AstNumberLiteral(node.location(), parseDouble(value));
        number.type(AstBuiltinType.FLOAT64);
      } else {
        number = new AstNumberLiteral(node.location(), parseLong(value));
        number.type(AstBuiltinType.INT64);
      }
      return number;
    } catch (NumberFormatException e) {
      errorConsumer.errorAt(node.location(), "Invalid number '%s': %s", value, e.getMessage());
      return node;
    }
  }

  @Override
  public AstExpression rewrite(AstStringLiteral node) {
    node.type(AstStringType.INSTANCE);
    return node;
  }

  @Override
  public AstExpression rewrite(AstVariableRef node) {
    return switch (node.name().entity()) {
      case "true" -> new AstBoolLiteral(node.location(), true);
      case "false" -> new AstBoolLiteral(node.location(), false);
      case "null" -> new AstNullLiteral(node.location());
      default -> node;
    };
  }

  @Override
  public AstExpression rewrite(AstFunctionRef node) {
    return super.rewrite(node);
  }

  private void checkIdentifier(Identifier name) {
    if (name == null) {
      return;
    }
    checkNameString(name.module(), name.location());
    checkNameString(name.entity(), name.location());
  }

  private void checkNameString(String name, Location location) {
    if (name == null) {
      return;
    }
    checkUnderscoreRules(name, location);
    if (Keyword.isReservedName(name)) {
      errorConsumer.errorAt(location, "Reserved word '%s' cannot be used as name", name);
    }
  }

  private void checkTypeName(Identifier name) {
    checkUnderscoreRules(name.entity(), name.location());
    if (Keyword.isReservedTypeName(name.entity())) {
      errorConsumer.errorAt(
          name.location(), "Reserved word '%s' cannot be used as type name", name.entity());
    }
  }

  private void checkTagName(Identifier name) {
    checkUnderscoreRules(name.entity(), name.location());
    if (Keyword.isReservedTypeName(name.entity())) {
      errorConsumer.errorAt(
          name.location(), "Reserved word '%s' cannot be used as tag", name.entity());
    }
  }

  private void checkUnderscoreRules(String name, Location location) {
    if (name.startsWith("_")) {
      errorConsumer.errorAt(location, "Identifier '%s' cannot start with '_'", name);
    }
    if (name.endsWith("_")) {
      errorConsumer.errorAt(location, "Identifier '%s' cannot end with '_'", name);
    }
    if (name.contains("__")) {
      errorConsumer.errorAt(location, "Identifier '%s' cannot contain '__'", name);
    }
  }
}
