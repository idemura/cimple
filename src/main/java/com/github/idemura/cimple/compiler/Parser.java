package com.github.idemura.cimple.compiler;

import java.util.ArrayList;
import java.util.List;

// Parses token stream, type checks and build the AST.
public class Parser {
  private final CompilerParams params;
  private final TokenStream tokens;

  Parser(CompilerParams params, TokenStream tokens) {
    this.params = params;
    this.tokens = tokens;
  }

  VisitorNode parse() {
    return parseModule();
  }

  private AstModule parseModule() {
    // TODO: 'module xxx;' syntax
    var module = new AstModule();
    while (!tokens.done()) {
      var tok = tokens.current();
      switch (tok.type()) {
        case FUNCTION:
          module.addFunction(parseFunction());
          break;
        case VARIABLE:
          module.addVariable(parseVariable());
          break;
        default:
          throw CompilerException.builder()
              .formatMessage("Invalid module definition")
              .setLocation(tok.location())
              .build();
      }
    }
    return module;
  }

  private AstFunction parseFunction() {
    tokens.take(TokenType.FUNCTION);
    var name = tokens.take(TokenType.IDENTIFIER);
    var parameters = parseParameters();
    var type = parseTypeRef();
    return new AstFunction(
        name.location(),
        name.value(),
        type == null ? BuiltinTypes.VOID : type,
        parameters,
        parseBlock());
  }

  private AstVariable parseVariable() {
    tokens.take(TokenType.VARIABLE);
    var name = tokens.take(TokenType.IDENTIFIER);
    var type = parseTypeRef();
    var init = tokens.takeIf(TokenType.ASSIGN) != null ? parseExpression() : null;
    tokens.take(TokenType.SEMICOLON);
    return new AstVariable(new VariableDef(name.location(), name.value(), type), init);
  }

  private AstBlock parseBlock() {
    var block = new AstBlock(tokens.current().location());
    tokens.take(TokenType.LCURLY);
    while (tokens.takeIf(TokenType.RCURLY) == null) {
      var current = tokens.current();
      switch (current.type()) {
        case RETURN:
          tokens.take();
          block.add(new AstReturn(current.location(), parseExpression()));
          tokens.take(TokenType.SEMICOLON);
          break;
        case IF:
          tokens.take();
          var condition = parseExpression();
          var thenBlock = parseBlock();
          var elseBlock = tokens.takeIf(TokenType.ELSE) != null ? parseBlock() : new AstBlock(null);
          block.add(new AstIfElse(current.location(), condition, thenBlock, elseBlock));
          break;
        case VARIABLE:
          block.add(parseVariable());
          break;
        default:
          // TODO: Should be an expression with a side effect
          block.add(new AstExpressionStmt(current.location(), parseExpression()));
          tokens.take(TokenType.SEMICOLON);
      }
    }
    return block;
  }

  private AstExpression parseExpression() {
    return parseExpressionComparison();
  }

  private AstExpression parseExpressionComparison() {
    AstExpression expr = parseExpressionAdditive();
    if (expr == null) {
      return null;
    }
    while (tokens.current().is(TokenType.CMP_LT) || tokens.current().is(TokenType.CMP_GT)) {
      var operator = tokens.take();
      var m = parseExpressionAdditive();
      if (m == null) {
        throw CompilerException.builder()
            .formatMessage("Expected expression after %s", operator)
            .setLocation(operator.location())
            .build();
      }
      expr = new AstFunctionApply(operator.location(), operator.toString(), List.of(expr, m));
    }
    return expr;
  }

  private AstExpression parseExpressionAdditive() {
    AstExpression expr = parseExpressionMultiple();
    if (expr == null) {
      return null;
    }
    while (tokens.current().is(TokenType.PLUS) || tokens.current().is(TokenType.MINUS)) {
      var operator = tokens.take();
      var m = parseExpressionMultiple();
      if (m == null) {
        throw CompilerException.builder()
            .formatMessage("Expected expression after %s", operator)
            .setLocation(operator.location())
            .build();
      }
      expr = new AstFunctionApply(operator.location(), operator.toString(), List.of(expr, m));
    }
    return expr;
  }

  private AstExpression parseExpressionMultiple() {
    AstExpression expr = parseExpressionPrimary();
    if (expr == null) {
      return null;
    }
    while (tokens.current().is(TokenType.ASTERISK) || tokens.current().is(TokenType.SLASH)) {
      var operator = tokens.take();
      var m = parseExpressionPrimary();
      if (m == null) {
        throw CompilerException.builder()
            .formatMessage("Expected expression after %s", operator)
            .setLocation(operator.location())
            .build();
      }
      expr = new AstFunctionApply(operator.location(), operator.toString(), List.of(expr, m));
    }
    return expr;
  }

  private AstExpression parseExpressionPrimary() {
    if (tokens.takeIf(TokenType.LPAREN) != null) {
      var e = parseExpression();
      tokens.take(TokenType.RPAREN);
      return e;
    }
    var t = tokens.take();
    switch (t.type()) {
      case TokenType.IDENTIFIER -> {
        if (tokens.current().is(TokenType.LPAREN)) {
          return new AstFunctionApply(t.location(), t.value(), parseExpressionList());
        } else {
          return new AstNameRef(t.location(), t.value());
        }
      }
      case TokenType.NUMBER -> {
        return new AstLiteral(t.location(), t.type(), t.value());
      }
      case TokenType.STRING -> {
        return new AstLiteral(t.location(), t.type(), t.value());
      }
      default ->
          throw CompilerException.builder()
              .formatMessage("Primary expression expected")
              .setLocation(t.location())
              .build();
    }
  }

  private List<AstExpression> parseExpressionList() {
    List<AstExpression> result = new ArrayList<>();
    tokens.take(TokenType.LPAREN);
    if (tokens.takeIf(TokenType.RPAREN) == null) {
      do {
        result.add(parseExpression());
      } while (expressionListHasNext());
    }
    return result;
  }

  private boolean expressionListHasNext() {
    if (tokens.takeIf(TokenType.COMMA) != null) {
      return true;
    } else if (tokens.takeIf(TokenType.RPAREN) != null) {
      return false;
    } else {
      throw CompilerException.builder()
          .formatMessage("Invalid function call: : or ) expected")
          .setLocation(tokens.current().location())
          .build();
    }
  }

  private List<VariableDef> parseParameters() {
    tokens.take(TokenType.LPAREN);
    List<VariableDef> params = new ArrayList<>();
    for (var t = tokens.takeIf(TokenType.IDENTIFIER);
        t != null;
        t = tokens.takeIf(TokenType.IDENTIFIER)) {
      params.add(new VariableDef(t.location(), t.value(), parseTypeRef()));
      if (tokens.takeIf(TokenType.COMMA) == null) {
        break;
      }
    }
    tokens.take(TokenType.RPAREN);
    return params;
  }

  private TypeRef parseTypeRef() {
    return TypeRef.ofName(tokens.take(TokenType.IDENTIFIER).value());
  }
}
