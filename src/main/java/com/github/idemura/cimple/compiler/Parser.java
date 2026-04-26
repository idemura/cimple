package com.github.idemura.cimple.compiler;

import static com.github.idemura.cimple.compiler.TokenType.*;

import java.util.ArrayList;
import java.util.List;

// Parses token stream, type checks and build the AST.
public class Parser {
  private final TokenStream tokens;

  Parser(TokenStream tokens) {
    this.tokens = tokens;
  }

  AstAbstractNode parse() {
    return parseModule();
  }

  private AstModule parseModule() {
    var module = new AstModule();
    parseModuleName(module);

    // TODO: Parse imports

    while (!tokens.done()) {
      var token = tokens.current();
      switch (token.keyword()) {
        case FUNCTION:
          module.addFunction(parseFunction());
          break;
        case TYPE:
          throw new UnsupportedOperationException();
        case VAR:
          module.addVariable(parseVariable());
          break;
        case CONST:
          module.addVariable(parseConst());
          break;
        default:
          throw CompilerException.builder()
              .formatMessage("Invalid module definition")
              .setLocation(token.location())
              .build();
      }
    }
    return module;
  }

  private void parseModuleName(AstModule module) {
    tokens.takeKeyword(MODULE);
    var tokenName = tokens.take(IDENTIFIER);
    module.setName(tokenName.value());
    module.setLocation(tokenName.location());
    tokens.take(SEMICOLON);
  }

  private AstFunction parseFunction() {
    tokens.takeKeyword(FUNCTION);
    var function = new AstFunction();
    parseFunctionQualifiedName(function);
    parseParameters(function);
    if (!tokens.current().is(LCURLY)) {
      function.setResultType(parseTypeRef());
    }
    function.setBlock(parseBlock());
    return function;
  }

  private AstVariable parseVariable() {
    tokens.takeKeyword(VAR);
    return parseVariableDefinition(true);
  }

  private AstVariable parseConst() {
    tokens.takeKeyword(CONST);
    return parseVariableDefinition(false);
  }

  private AstVariable parseVariableDefinition(boolean isMutable) {
    var variable = new AstVariable();
    variable.setMutable(isMutable);
    var name = tokens.take(IDENTIFIER);
    variable.setLocation(name.location());
    variable.setName(name.value());
    if (tokens.current().is(IDENTIFIER)) {
      variable.setTypeRef(parseTypeRef());
    }
    if (tokens.takeIf(ASSIGN)) {
      variable.setInit(parseExpression());
    }
    tokens.take(SEMICOLON);
    return variable;
  }

  private AstBlock parseBlock() {
    var b = new AstBlock();
    tokens.take(LCURLY);
    while (!tokens.takeIf(RCURLY)) {
      var current = tokens.current();
      switch (current.keyword()) {
        case VAR:
          b.add(parseVariable());
          break;
        case CONST:
          b.add(parseConst());
          break;
        case RETURN:
          b.add(parseReturn());
          break;
        case IF:
          b.add(parseIf());
          break;
        case FOR:
          b.add(parseFor());
          break;
        case MATCH:
          throw new UnsupportedOperationException();
        case GOTO:
          b.add(parseGoto());
          break;
        default:
          // TODO: Should be an expression with a side effect
          b.add(parseExpressionStatement());
          break;
      }
    }
    return b;
  }

  private AstAbstractStatement parseReturn() {
    var r = new AstReturn();
    var keyword = tokens.takeKeyword(RETURN);
    r.setLocation(keyword.location());
    r.setExpression(parseExpression());
    tokens.take(SEMICOLON);
    return r;
  }

  private AstAbstractStatement parseIf() {
    var ifNode = new AstIf();
    var keyword = tokens.takeKeyword(IF);
    ifNode.setLocation(keyword.location());
    do {
      ifNode.addIf(parseExpression(), parseBlock());
    } while (tokens.takeKeywordIf(ELIF));
    if (tokens.takeKeywordIf(ELSE)) {
      ifNode.setElseBlock(parseBlock());
    }
    return ifNode;
  }

  private AstAbstractStatement parseFor() {
    var forNode = new AstFor();
    var keyword = tokens.takeKeyword(FOR);
    forNode.setLocation(keyword.location());
    if (!tokens.current().is(LCURLY)) {
      forNode.setCondition(parseExpression());
    }
    forNode.setBlock(parseBlock());
    return forNode;
  }

  private AstAbstractStatement parseGoto() {
    var gotoNode = new AstGoto();
    var keyword = tokens.takeKeyword(GOTO);
    gotoNode.setLocation(keyword.location());
    gotoNode.setLabel(tokens.take(IDENTIFIER).value());
    tokens.take(SEMICOLON);
    return gotoNode;
  }

  private AstAbstractStatement parseExpressionStatement() {
    var e = new AstExpressionStatement();
    e.setLocation(tokens.current().location());
    e.setExpression(parseExpression());
    tokens.take(SEMICOLON);
    return e;
  }

  private AstAbstractExpression parseExpression() {
    return parseExpressionComparison();
  }

  private AstAbstractExpression parseExpressionComparison() {
    AstAbstractExpression expr = parseExpressionAdditive();
    if (expr == null) {
      return null;
    }
    while (tokens.current().is(CMP_LT) || tokens.current().is(CMP_GT)) {
      var operator = tokens.take();
      var m = parseExpressionAdditive();
      if (m == null) {
        throw CompilerException.builder()
            .formatMessage("Expected expression after %s", operator)
            .setLocation(operator.location())
            .build();
      }
      // expr = new AstFunctionApply(operator.location(), operator.toString(), List.of(expr, m));
      expr = new AstFunctionApply();
    }
    return expr;
  }

  private AstAbstractExpression parseExpressionAdditive() {
    AstAbstractExpression expr = parseExpressionMultiple();
    if (expr == null) {
      return null;
    }
    while (tokens.current().is(PLUS) || tokens.current().is(MINUS)) {
      var operator = tokens.take();
      var m = parseExpressionMultiple();
      if (m == null) {
        throw CompilerException.builder()
            .formatMessage("Expected expression after %s", operator)
            .setLocation(operator.location())
            .build();
      }
      // expr = new AstFunctionApply(operator.location(), operator.toString(), List.of(expr, m));
      expr = new AstFunctionApply();
    }
    return expr;
  }

  private AstAbstractExpression parseExpressionMultiple() {
    AstAbstractExpression expr = parseExpressionPrimary();
    if (expr == null) {
      return null;
    }
    while (tokens.current().is(ASTERISK) || tokens.current().is(SLASH)) {
      var operator = tokens.take();
      var m = parseExpressionPrimary();
      if (m == null) {
        throw CompilerException.builder()
            .formatMessage("Expected expression after %s", operator)
            .setLocation(operator.location())
            .build();
      }
      // expr = new AstFunctionApply(operator.location(), operator.toString(), List.of(expr, m));
      expr = new AstFunctionApply();
    }
    return expr;
  }

  private AstAbstractExpression parseExpressionPrimary() {
    if (tokens.takeIf(LPAREN)) {
      var e = parseExpression();
      tokens.take(RPAREN);
      return e;
    }
    var t = tokens.take();
    switch (t.type()) {
      case IDENTIFIER -> {
        if (tokens.current().is(LPAREN)) {
          var a = new AstFunctionApply();
          return a;
        } else {
          var n = new AstNameRef(t.value());
          n.setLocation(t.location());
          return n;
        }
      }
      case NUMBER, STRING -> {
        var l = new AstLiteral(t.type(), t.value());
        l.setLocation(t.location());
        return l;
      }
      default ->
          throw CompilerException.builder()
              .formatMessage("Primary expression expected")
              .setLocation(t.location())
              .build();
    }
  }

  private List<AstAbstractExpression> parseExpressionList() {
    List<AstAbstractExpression> result = new ArrayList<>();
    tokens.take(LPAREN);
    if (!tokens.takeIf(RPAREN)) {
      do {
        result.add(parseExpression());
      } while (expressionListHasNext());
    }
    return result;
  }

  private boolean expressionListHasNext() {
    if (tokens.takeIf(COMMA)) {
      return true;
    } else if (tokens.takeIf(RPAREN)) {
      return false;
    } else {
      throw CompilerException.builder()
          .formatMessage("Invalid function call: : or ) expected")
          .setLocation(tokens.current().location())
          .build();
    }
  }

  private void parseFunctionQualifiedName(AstFunction function) {
    function.setLocation(tokens.current().location());
    if (tokens.next() == PERIOD) {
      function.setBoundTypeName(tokens.take(IDENTIFIER).value());
      tokens.take(PERIOD);
    }
    function.setName(tokens.take(IDENTIFIER).value());
  }

  private void parseParameters(AstFunction function) {
    tokens.take(LPAREN);
    if (!tokens.current().is(RPAREN)) {
      do {
        var variable = new VariableDef();
        var tokenName = tokens.take(IDENTIFIER);
        variable.setName(tokenName.value());
        variable.setLocation(tokenName.location());
        if (tokens.current().is(IDENTIFIER)) {
          variable.setTypeRef(parseTypeRef());
        }
        function.addParameter(variable);
      } while (tokens.takeIf(COMMA));
    }
    tokens.take(RPAREN);
  }

  private TypeRef parseTypeRef() {
    return new TypeRef(tokens.take(IDENTIFIER).value());
  }
}
