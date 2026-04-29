package com.github.idemura.cimple.compiler.parser;

import static com.github.idemura.cimple.compiler.tokens.TokenType.*;
import static java.lang.Double.parseDouble;
import static java.lang.Long.parseLong;

import com.github.idemura.cimple.compiler.BuiltinType;
import com.github.idemura.cimple.compiler.CompilerException;
import com.github.idemura.cimple.compiler.TypeRef;
import com.github.idemura.cimple.compiler.VariableDef;
import com.github.idemura.cimple.compiler.ast.*;
import com.github.idemura.cimple.compiler.tokens.TokenStream;
import java.util.ArrayList;
import java.util.List;

// Parses token stream, type checks and build the AST.
public class Parser {
  private final TokenStream tokens;

  public Parser(TokenStream tokens) {
    this.tokens = tokens;
  }

  public AstModule parse() {
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
          module.addType(parseType());
          break;
        case VAR:
          module.addVariable(parseVariable(true));
          break;
        case CONST:
          module.addVariable(parseVariable(false));
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

  private AstType parseType() {
    tokens.takeKeyword(TYPE);
    return switch (tokens.current().keyword()) {
      case FUNCTION -> parseTypeFunction();
      case STRUCT -> parseTypeStruct();
      case UNION -> throw new UnsupportedOperationException();
      case ALIAS -> parseTypeAlias();
      default ->
          throw CompilerException.builder()
              .formatMessage("Expected type declaration kind after type")
              .setLocation(tokens.current().location())
              .build();
    };
  }

  private AstTypeStruct parseTypeStruct() {
    var type = new AstTypeStruct();
    tokens.takeKeyword(STRUCT);
    var name = tokens.take(IDENTIFIER);
    type.setLocation(name.location());
    type.setName(name.value());
    tokens.take(LCURLY);
    while (!tokens.takeIf(RCURLY)) {
      type.addField(parseVariable(tokens.current().keyword() != CONST));
    }
    return type;
  }

  private AstTypeAlias parseTypeAlias() {
    var type = new AstTypeAlias();
    tokens.takeKeyword(ALIAS);
    var name = tokens.take(IDENTIFIER);
    type.setLocation(name.location());
    type.setName(name.value());
    tokens.take(ASSIGN);
    type.setBaseTypeRef(parseTypeRef());
    tokens.take(SEMICOLON);
    return type;
  }

  private AstTypeFunction parseTypeFunction() {
    var type = new AstTypeFunction();
    var header = parseFunctionHeader();
    type.setHeader(header);
    type.setLocation(header.getLocation());
    tokens.take(SEMICOLON);
    return type;
  }

  private void parseModuleName(AstModule module) {
    tokens.takeKeyword(MODULE);
    var tokenName = tokens.take(IDENTIFIER);
    module.setName(tokenName.value());
    module.setLocation(tokenName.location());
    tokens.take(SEMICOLON);
  }

  private AstFunction parseFunction() {
    var function = new AstFunction();
    var header = parseFunctionHeader();
    function.setHeader(header);
    function.setLocation(header.getLocation());
    function.setBlock(parseBlock());
    return function;
  }

  private AstVariable parseVariable(boolean mutable) {
    tokens.takeKeyword(mutable ? VAR : CONST);
    var variable = new AstVariable();
    if (mutable) {
      variable.setBit(AstVariable.MUTABLE);
    }
    var name = tokens.take(IDENTIFIER);
    variable.setLocation(name.location());
    variable.setName(name.value());
    if (tokens.current().is(IDENTIFIER)) {
      variable.setTypeRef(parseTypeRef());
    }
    if (tokens.takeIf(ASSIGN)) {
      variable.setExpression(parseExpression());
    }
    tokens.take(SEMICOLON);
    return variable;
  }

  private AstBlock parseBlock() {
    var b = new AstBlock();
    tokens.take(LCURLY);
    while (!tokens.takeIf(RCURLY)) {
      b.add(parseStatement());
    }
    return b;
  }

  private AstStatement parseStatement() {
    var current = tokens.current();
    return switch (current.keyword()) {
      case VAR -> parseVariable(true);
      case CONST -> parseVariable(false);
      case RETURN -> parseReturn();
      case IF -> parseIf();
      case FOR -> parseFor();
      case DEFER -> parseDefer();
      case MATCH -> throw new UnsupportedOperationException();
      case GOTO -> parseGoto();
      default -> parseExpressionStatement();
    };
  }

  private AstStatement parseReturn() {
    var stmt = new AstReturn();
    var keyword = tokens.takeKeyword(RETURN);
    stmt.setLocation(keyword.location());
    stmt.setExpression(parseExpression());
    tokens.take(SEMICOLON);
    return stmt;
  }

  private AstStatement parseIf() {
    var stmt = new AstIf();
    var keyword = tokens.takeKeyword(IF);
    stmt.setLocation(keyword.location());
    stmt.addIf(parseExpression(), parseBlock());
    while (tokens.takeKeywordIf(ELSE)) {
      if (tokens.takeKeywordIf(IF)) {
        stmt.addIf(parseExpression(), parseBlock());
      } else {
        stmt.setElseBlock(parseBlock());
        break;
      }
    }
    return stmt;
  }

  private AstStatement parseFor() {
    var stmt = new AstFor();
    var keyword = tokens.takeKeyword(FOR);
    stmt.setLocation(keyword.location());
    if (tokens.current().keyword() == VAR) {
      stmt.setInit(parseVariable(true));
    }
    // Condition must be present, even if simple "true".
    stmt.setCondition(parseExpression());
    if (tokens.takeIf(SEMICOLON)) {
      stmt.setIncrement(parseExpression());
    }
    stmt.setBlock(parseBlock());
    return stmt;
  }

  private AstStatement parseGoto() {
    var stmt = new AstGoto();
    var keyword = tokens.takeKeyword(GOTO);
    stmt.setLocation(keyword.location());
    stmt.setLabel(tokens.take(IDENTIFIER).value());
    tokens.take(SEMICOLON);
    return stmt;
  }

  private AstStatement parseDefer() {
    var stmt = new AstDefer();
    var keyword = tokens.takeKeyword(DEFER);
    stmt.setLocation(keyword.location());
    stmt.setExpression(parseExpression());
    tokens.take(SEMICOLON);
    return stmt;
  }

  private AstStatement parseExpressionStatement() {
    var e = new AstExpressionStatement();
    e.setLocation(tokens.current().location());
    e.setExpression(parseExpression());
    tokens.take(SEMICOLON);
    return e;
  }

  private AstExpression parseExpression() {
    return parseExpressionComparison();
  }

  private AstExpression parseExpressionComparison() {
    AstExpression expr = parseExpressionAdditive();
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

  private AstExpression parseExpressionAdditive() {
    AstExpression expr = parseExpressionMultiple();
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

  private AstExpression parseExpressionMultiple() {
    AstExpression expr = parseExpressionPrimary();
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

  private AstExpression parseExpressionPrimary() {
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
      case NUMBER -> {
        var l = new AstLiteral();
        if (t.value().contains(".")) {
          l.setValue(parseDouble(t.value()));
          l.setType(BuiltinType.FLOAT64);
        } else {
          l.setValue(parseLong(t.value()));
          l.setType(BuiltinType.INT64);
        }
        l.setLocation(t.location());
        return l;
      }
      case STRING -> {
        var l = new AstLiteral();
        l.setValue(t.value());
        l.setType(BuiltinType.STRING);
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

  private List<AstExpression> parseExpressionList() {
    List<AstExpression> result = new ArrayList<>();
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

  private AstFunctionHeader parseFunctionHeader() {
    tokens.takeKeyword(FUNCTION);
    var header = new AstFunctionHeader();
    header.setLocation(tokens.current().location());
    String boundTypeName = null;
    if (tokens.next() == PERIOD) {
      boundTypeName = tokens.take(IDENTIFIER).value();
      tokens.take(PERIOD);
    }
    header.setBoundTypeName(boundTypeName);
    header.setName(tokens.take(IDENTIFIER).value());
    var parameters = parseParameters();
    header.setParameters(parameters);
    if (!tokens.current().is(LCURLY) && !tokens.current().is(SEMICOLON)) {
      header.setResultType(parseTypeRef());
    }
    return header;
  }

  private List<VariableDef> parseParameters() {
    var parameters = new ArrayList<VariableDef>();
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
        parameters.add(variable);
      } while (tokens.takeIf(COMMA));
    }
    tokens.take(RPAREN);
    return parameters;
  }

  private TypeRef parseTypeRef() {
    return new TypeRef(tokens.take(IDENTIFIER).value());
  }
}
