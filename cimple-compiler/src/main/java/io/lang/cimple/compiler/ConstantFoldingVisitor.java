package io.lang.cimple.compiler;

import java.util.function.LongSupplier;

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
    LongSupplier nextValue = () -> 0L;
    for (var variant : node.variants()) {
      Long value;
      if (variant.expression() == null) {
        try {
          // TODO: Check overflow of the base type, not int64!
          value = nextValue.getAsLong();
        } catch (ArithmeticException e) {
          errorConsumer.errorAt(
              variant.tag().location(),
              "Enum %s variant %s value overflows base type",
              node.name(),
              variant.tag());
          break;
        }
      } else {
        var literal = (AstNumberLiteral) variant.expression();
        // Type checking has already guaranteed that value expressions are integer literals.
        value = (Long) literal.value();
      }
      variant.value(value);
      hasZeroValue |= value == 0;
      nextValue = () -> Math.addExact(value, 1);
    }
    if (!hasZeroValue) {
      errorConsumer.errorAt(
          node.location(), "Enum '%s' must define a variant with value 0", node.name());
    }
  }
}
