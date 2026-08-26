package com.github.idemura.cimple.compiler.semantics;

import static java.lang.Double.parseDouble;
import static java.lang.Long.parseLong;

import com.github.idemura.cimple.compiler.ErrorConsumer;
import com.github.idemura.cimple.compiler.Identifier;
import com.github.idemura.cimple.compiler.Location;
import com.github.idemura.cimple.compiler.ast.AstAssign;
import com.github.idemura.cimple.compiler.ast.AstBoolLiteral;
import com.github.idemura.cimple.compiler.ast.AstBuiltinType;
import com.github.idemura.cimple.compiler.ast.AstCall;
import com.github.idemura.cimple.compiler.ast.AstCompoundAssign;
import com.github.idemura.cimple.compiler.ast.AstEntityRef;
import com.github.idemura.cimple.compiler.ast.AstExpression;
import com.github.idemura.cimple.compiler.ast.AstExpressionRewriteVisitor;
import com.github.idemura.cimple.compiler.ast.AstFieldAccess;
import com.github.idemura.cimple.compiler.ast.AstFunction;
import com.github.idemura.cimple.compiler.ast.AstFunctionHeader;
import com.github.idemura.cimple.compiler.ast.AstFunctionType;
import com.github.idemura.cimple.compiler.ast.AstLocal;
import com.github.idemura.cimple.compiler.ast.AstModule;
import com.github.idemura.cimple.compiler.ast.AstNew;
import com.github.idemura.cimple.compiler.ast.AstNullLiteral;
import com.github.idemura.cimple.compiler.ast.AstNumberLiteral;
import com.github.idemura.cimple.compiler.ast.AstRecordType;
import com.github.idemura.cimple.compiler.ast.AstStringLiteral;
import com.github.idemura.cimple.compiler.ast.AstStringType;
import com.github.idemura.cimple.compiler.ast.AstTypeRef;
import com.github.idemura.cimple.compiler.ast.AstUnionType;
import com.github.idemura.cimple.compiler.ast.AstVariable;
import java.util.HashMap;

// Runs AST checks and rewrites that do not require name resolution:
//  - Validates identifiers
//  - Marks parameters and locals
//  - Validates method object parameters
//  - Sets missing function result types to void
//  - Checks that variables have either a type or an initializer
//  - Checks duplicate function parameters, record fields, and union variants
//  - Normalizes builtin type aliases such as int and float
//  - Marks method-call syntax
//  - Rejects nested assignments
//  - Types literal nodes
class PreprocessVisitor extends AstExpressionRewriteVisitor {
  private final ReservedWords reservedWords;
  private final ErrorConsumer errorConsumer;
  private final NormalizeTypeNameVisitor normalizeTypeNameVisitor = new NormalizeTypeNameVisitor();
  private AstModule module;

  PreprocessVisitor(ReservedWords reservedWords, ErrorConsumer errorConsumer) {
    this.reservedWords = reservedWords;
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
    checkQualifiedName(node.name(), true, node.location());
    checkObjectParameter(node.name(), node.header());
    super.visit(node);
    normalizeMethodObjectName(node);
  }

  private void normalizeMethodObjectName(AstFunction node) {
    if (node.name().typeName() == null
        || !(node.header().objectType() instanceof AstTypeRef typeRef)) {
      return;
    }
    // Keep the method-map key in sync with normalized builtin type names such as int -> int64.
    node.name(node.name().withType(typeRef.name().typeName()));
  }

  private void checkObjectParameter(Identifier functionName, AstFunctionHeader header) {
    // Methods must have exactly one object parameter: the only parameter without an
    // explicit type. Functions must not have any untyped parameters.
    var parameters = header.parameters();
    if (header.objectType() != null) {
      var objectIndex = -1;
      var invalid = false;
      for (int i = 0; i < parameters.size(); i++) {
        if (parameters.get(i).type() == null) {
          if (objectIndex >= 0) {
            errorConsumer.errorAt(
                header.location(), "Method '%s': multiple object parameters", functionName);
            invalid = true;
            break;
          }
          objectIndex = i;
        }
      }
      if (!invalid && objectIndex < 0) {
        errorConsumer.errorAt(
            header.location(), "Method '%s': missing the object parameter", functionName);
      } else {
        header.objectIndex(objectIndex);
        parameters.get(objectIndex).type(header.objectType());
      }
    } else {
      for (var parameter : parameters) {
        if (parameter.type() == null) {
          errorConsumer.errorAt(
              parameter.location(),
              "Function '%s' cannot have object parameter '%s'",
              functionName,
              parameter.name());
        }
      }
    }
  }

  @Override
  protected void visit(AstVariable node) {
    checkQualifiedName(node.name(), false, node.location());
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
    checkQualifiedName(node.name(), true, node.location());
    checkObjectParameter(node.name(), node.header());
    super.visit(node);
  }

  @Override
  protected void visit(AstLocal node) {
    node.variable().setBit(AstVariable.LOCAL);
    super.visit(node);
  }

  @Override
  protected void visit(AstRecordType node) {
    checkQualifiedName(node.name(), false, node.location());
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
    checkQualifiedName(node.name(), false, node.location());
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
  public AstExpression rewrite(AstCall node) {
    if (node.function() instanceof AstFieldAccess fieldAccess) {
      // Method call syntax starts as field access; later resolution binds it to a method using
      // the object's type.
      fieldAccess.method(true);
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
      checkQualifiedName(node.name(), false, node.location());
    }
    return newNode;
  }

  private void checkQualifiedName(Identifier name, boolean method, Location location) {
    if (!name.isBuiltin()) {
      checkName(name.moduleName(), location);
      checkTypeName(name.typeName(), method, location);
      checkName(name.entityName(), location);
    }
  }

  private void checkName(String name, Location location) {
    if (name == null) {
      return;
    }
    checkUnderscoreRules(name, location);
    if (reservedWords.isReservedName(name)) {
      errorConsumer.errorAt(location, "Reserved word '%s' cannot be used as a name", name);
    }
  }

  private void checkTypeName(String name, boolean method, Location location) {
    if (name == null) {
      return;
    }
    checkUnderscoreRules(name, location);
    // Allow methods for reserved type names.
    if (method && reservedWords.isReservedTypeName(name)) {
      return;
    }
    if (reservedWords.isReservedName(name)) {
      errorConsumer.errorAt(location, "Reserved word '%s' cannot be used as a type name", name);
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
