package com.github.idemura.cimple.compiler.semantics;

import static com.github.idemura.cimple.compiler.parser.Parser.parseCode;
import static org.junit.jupiter.api.Assertions.*;

import com.github.idemura.cimple.compiler.InMemoryErrorConsumer;
import com.github.idemura.cimple.compiler.ast.AstCall;
import com.github.idemura.cimple.compiler.ast.AstEntityRef;
import com.github.idemura.cimple.compiler.ast.AstExpression;
import com.github.idemura.cimple.compiler.ast.AstExpressionStatement;
import com.github.idemura.cimple.compiler.ast.AstFunction;
import com.github.idemura.cimple.compiler.ast.AstModule;
import com.github.idemura.cimple.compiler.ast.AstType;
import com.github.idemura.cimple.compiler.ast.AstVariable;
import com.github.idemura.cimple.compiler.parser.Keyword;
import java.util.List;
import org.junit.jupiter.api.Test;

class NameResolutionVisitorTest {
  private static List<AstFunction> moduleFunctions(AstModule module) {
    return module.definitions().stream()
        .filter(AstFunction.class::isInstance)
        .map(AstFunction.class::cast)
        .toList();
  }

  private static List<AstVariable> moduleVariables(AstModule module) {
    return module.definitions().stream()
        .filter(AstVariable.class::isInstance)
        .map(AstVariable.class::cast)
        .toList();
  }

  private static List<AstType> types(AstModule module) {
    return module.definitions().stream()
        .filter(AstType.class::isInstance)
        .map(AstType.class::cast)
        .toList();
  }

  private static AstExpression extractBodyExpression(AstFunction function) {
    return ((AstExpressionStatement) function.getBlock().statements().get(0)).getExpression();
  }

  @Test
  void testResolveValuesAndTypes() {
    var code =
        """
        module test;

        type record R {}

        var x;

        function f() {
          x;
        }

        function g() {
          f();
        }
        """;

    var errorConsumer = new InMemoryErrorConsumer();
    var module = parseCode(code, errorConsumer);
    var nameMap = new NameMap();
    module.accept(new PreprocessVisitor(nameMap, Keyword.valueList(), errorConsumer));
    module.accept(new NameResolutionVisitor(nameMap, errorConsumer));

    assertEquals(List.of(), errorConsumer.getErrors());

    var varX = moduleVariables(module).get(0);
    var functions = moduleFunctions(module);
    {
      var expr = extractBodyExpression(functions.get(0));
      var entityRef = (AstEntityRef) expr;
      assertSame(varX, entityRef.getEntity());
    }
    {
      var expr = extractBodyExpression(functions.get(1));
      var call = (AstCall) expr;
      var entityRef = (AstEntityRef) call.getFunction();
      assertSame(moduleFunctions(module).get(0), entityRef.getEntity());
    }
  }
}
