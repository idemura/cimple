package io.lang.cimple.compiler;

import static java.lang.Double.parseDouble;
import static java.lang.Long.parseLong;

import io.lang.cimple.compiler.ast.AstAssign;
import io.lang.cimple.compiler.ast.AstBoolLiteral;
import io.lang.cimple.compiler.ast.AstBuiltinType;
import io.lang.cimple.compiler.ast.AstCompoundAssign;
import io.lang.cimple.compiler.ast.AstEntityRef;
import io.lang.cimple.compiler.ast.AstEnumType;
import io.lang.cimple.compiler.ast.AstExpression;
import io.lang.cimple.compiler.ast.AstExpressionRewriteVisitor;
import io.lang.cimple.compiler.ast.AstFunction;
import io.lang.cimple.compiler.ast.AstFunctionHeader;
import io.lang.cimple.compiler.ast.AstFunctionType;
import io.lang.cimple.compiler.ast.AstLocal;
import io.lang.cimple.compiler.ast.AstModule;
import io.lang.cimple.compiler.ast.AstNew;
import io.lang.cimple.compiler.ast.AstNullLiteral;
import io.lang.cimple.compiler.ast.AstNumberLiteral;
import io.lang.cimple.compiler.ast.AstStringLiteral;
import io.lang.cimple.compiler.ast.AstStringType;
import io.lang.cimple.compiler.ast.AstStructType;
import io.lang.cimple.compiler.ast.AstTypeRef;
import io.lang.cimple.compiler.ast.AstUnionType;
import io.lang.cimple.compiler.ast.AstVariable;
import java.util.HashMap;

// Runs AST checks and rewrites that do not require name resolution:
//  - Validates identifiers
//  - Marks parameters and locals
//  - Sets missing function result types to void
//  - Checks that variables have either a type or an initializer
//  - Checks duplicate function parameters, struct fields, union variants, and enum variants.
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
    checkIdentifier(node.name(), node.location());
    checkParameterTypes(node.name(), node.header());
    super.visit(node);
  }

  private void checkParameterTypes(Identifier functionName, AstFunctionHeader header) {
    for (var parameter : header.parameters()) {
      if (parameter.type() == null) {
        errorConsumer.errorAt(
            parameter.location(),
            "Function '%s' parameter '%s' must have a type",
            functionName,
            parameter.name());
      }
    }
  }

  @Override
  protected void visit(AstVariable node) {
    checkIdentifier(node.name(), node.location());
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
    checkIdentifier(node.name(), node.location());
    checkParameterTypes(node.name(), node.header());
    super.visit(node);
  }

  @Override
  protected void visit(AstLocal node) {
    node.variable().setBit(AstVariable.LOCAL);
    super.visit(node);
  }

  @Override
  protected void visit(AstStructType node) {
    checkIdentifier(node.name(), node.location());
    var fieldMap = new HashMap<String, AstVariable>();
    for (var field : node.fields()) {
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
    checkIdentifier(node.name(), node.location());
    var variantMap = new HashMap<String, AstUnionType.Variant>();
    for (var variant : node.variants()) {
      checkTagName(variant.tag(), node.location());
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

  @Override
  protected void visit(AstEnumType node) {
    checkIdentifier(node.name(), node.location());
    var variantMap = new HashMap<String, AstEnumType.Variant>();
    for (var variant : node.variants()) {
      checkTagName(variant.tag(), node.location());
      var existing = variantMap.putIfAbsent(variant.tag(), variant);
      if (existing != null) {
        errorConsumer.errorAt(
            variant.location(),
            "Duplicate enum variant '%s'. First defined at %s.",
            variant.tag(),
            existing.location());
      }
    }
    super.visit(node);
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
    node.type(AstStringType.INSTANCE);
    return node;
  }

  @Override
  public AstExpression rewrite(AstEntityRef node) {
    var newNode =
        switch (node.name().entity()) {
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
      checkIdentifier(node.name(), node.location());
    }
    return newNode;
  }

  private void checkIdentifier(Identifier ident, Location location) {
    if (!ident.isBuiltin()) {
      checkName(ident.module(), location);
      var type = ident.type();
      if (type != null) {
        checkUnderscoreRules(type, location);
        if (Keyword.isReservedTypeName(type)) {
          errorConsumer.errorAt(location, "Reserved word '%s' cannot be used as type name", ident);
        }
      }
      checkName(ident.entity(), location);
    }
  }

  private void checkTagName(String tag, Location location) {
    checkUnderscoreRules(tag, location);
    if (Keyword.isReservedTypeName(tag)) {
      errorConsumer.errorAt(location, "Reserved word '%s' cannot be used as tag", tag);
    }
  }

  private void checkName(String name, Location location) {
    if (name == null) {
      return;
    }
    checkUnderscoreRules(name, location);
    if (Keyword.isReservedName(name)) {
      errorConsumer.errorAt(location, "Reserved word '%s' cannot be used as name", name);
    }
  }

  private void checkUnderscoreRules(String name, Location location) {
    if (module.builtin()) {
      // Exception for builtin modules.
      return;
    }
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
