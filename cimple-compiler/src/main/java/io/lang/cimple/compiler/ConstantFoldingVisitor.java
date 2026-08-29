package io.lang.cimple.compiler;

import io.lang.cimple.compiler.ast.AstEnumType;
import io.lang.cimple.compiler.ast.AstExpressionRewriteVisitor;
import io.lang.cimple.compiler.ast.AstNumberLiteral;
import io.lang.cimple.compiler.ast.AstTypeHolder;

// Folds compile-time expressions and assigns enum variant values.
class ConstantFoldingVisitor extends AstExpressionRewriteVisitor {
  private final ErrorConsumer errorConsumer;

  ConstantFoldingVisitor(ErrorConsumer errorConsumer) {
    this.errorConsumer = errorConsumer;
  }

  @Override
  protected void visit(AstTypeHolder node) {}

  @Override
  protected void visit(AstEnumType node) {
    super.visit(node);

    // Assign values to the variants.
    var hasZeroValue = false;
    var nextValue = 0L;
    for (var variant : node.variants()) {
      Long value;
      if (variant.valueExpression() == null) {
        value = nextValue;
      } else {
        var literal = (AstNumberLiteral) variant.valueExpression();
        // Type checking has already guaranteed that value expressions are integer literals.
        value = (Long) literal.value();
      }
      variant.value(value);
      hasZeroValue |= value == 0;
      try {
        // TODO: Check overflow of the base type, not int64!
        nextValue = Math.addExact(value, 1);
      } catch (ArithmeticException e) {
        errorConsumer.errorAt(
            variant.location(), "Enum value after variant '%s' overflows base type", variant.tag());
        break;
      }
    }
    if (!hasZeroValue) {
      errorConsumer.errorAt(
          node.location(), "Enum '%s' must define a variant with value 0", node.name());
    }
  }
}
