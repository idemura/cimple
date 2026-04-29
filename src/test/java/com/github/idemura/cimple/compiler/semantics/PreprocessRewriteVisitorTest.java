package com.github.idemura.cimple.compiler.semantics;

import static org.junit.jupiter.api.Assertions.*;

import com.github.idemura.cimple.compiler.ast.AstBuiltinType;
import com.github.idemura.cimple.compiler.ast.AstDefer;
import com.github.idemura.cimple.compiler.ast.AstFor;
import com.github.idemura.cimple.compiler.ast.AstIf;
import com.github.idemura.cimple.compiler.ast.AstLiteral;
import com.github.idemura.cimple.compiler.ast.AstReturn;
import com.github.idemura.cimple.compiler.ast.AstVariable;
import com.github.idemura.cimple.compiler.parser.Parser;
import com.github.idemura.cimple.compiler.tokens.Tokenizer;
import org.junit.jupiter.api.Test;

class PreprocessRewriteVisitorTest {
  @Test
  void testRewriteTrueFalseNullLiterals() {
    var code =
        """
        module test_semantics;

        function f() {
          if true {
          }
          defer null;
          var x = false;
          for var i = null; true; false {
          }
          return true;
        }
        """;

    var module = new Parser(new Tokenizer(code).split()).parse();
    module.accept(new PreprocessRewriteVisitor());

    var statements = module.getFunctions().get(0).getBlock().getStatements();
    int i = 0;
    assertSame(AstLiteral.TRUE, ((AstIf) statements.get(i++)).getConditions().get(0));
    assertSame(AstLiteral.NULL, ((AstDefer) statements.get(i++)).getExpression());
    assertSame(AstLiteral.FALSE, ((AstVariable) statements.get(i++)).getExpression());
    {
      var stmtFor = (AstFor) statements.get(i++);
      assertSame(AstLiteral.NULL, stmtFor.getInit().getExpression());
      assertSame(AstLiteral.TRUE, stmtFor.getCondition());
      assertSame(AstLiteral.FALSE, stmtFor.getIncrement());
    }
    assertSame(AstLiteral.TRUE, ((AstReturn) statements.get(i++)).getExpression());
    assertEquals(AstBuiltinType.NULL, AstLiteral.NULL.getType());
  }
}
