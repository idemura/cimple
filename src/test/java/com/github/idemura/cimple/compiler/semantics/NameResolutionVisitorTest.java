package com.github.idemura.cimple.compiler.semantics;

import static com.github.idemura.cimple.compiler.parser.Parser.parseCode;

import com.github.idemura.cimple.compiler.InMemoryErrorConsumer;
import com.github.idemura.cimple.compiler.ast.AstFunction;
import com.github.idemura.cimple.compiler.ast.AstModule;
import com.github.idemura.cimple.compiler.ast.AstType;
import com.github.idemura.cimple.compiler.ast.AstVariable;
import com.github.idemura.cimple.compiler.parser.Keyword;
import java.util.List;
import org.junit.jupiter.api.Test;

class NameResolutionVisitorTest {
  private static List<AstFunction> functions(AstModule module) {
    return module.definitions().stream()
        .filter(AstFunction.class::isInstance)
        .map(AstFunction.class::cast)
        .toList();
  }

  private static List<AstVariable> variables(AstModule module) {
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

  @Test
  void testResolveValuesAndTypes() {
    var code =
        """
        module test;

        var x;

        function f() {
          x;
        }

        function g() {
          f();
        }

        type record R {
        }
        """;

    var errorConsumer = new InMemoryErrorConsumer();
    var module = parseCode(code, errorConsumer);
    var nameMap = new NameMap();
    module.accept(new PreprocessVisitor(nameMap, Keyword.valueList(), errorConsumer));
    module.accept(new NameResolutionVisitor(nameMap, errorConsumer));

    // assertEquals(List.of(), errorConsumer.getErrors());
    //
    // var variable = variables(module).get(0);
    // {
    //   var function = functions(module).get(0);
    //   var expr = ((AstExpressionStatement)
    // function.getBlock().statements().get(0)).getExpression();
    //   var name = (AstEntityRef) expr;
    //   // assertSame(variable, name.getVariable());
    // }
    // {
    //   var function = functions(module).get(1);
    //   var expr = ((AstExpressionStatement)
    // function.getBlock().statements().get(0)).getExpression();
    //   var call = (AstCall) expr;
    //   var name = (AstEntityRef) call.getFunction();
    //   // assertSame(functions(module).get(0), name.getFunction());
    // }
  }
}
