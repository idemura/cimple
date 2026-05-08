package com.github.idemura.cimple.compiler.parser;

import static com.github.idemura.cimple.compiler.parser.Keyword.*;
import static com.github.idemura.cimple.compiler.parser.TokenType.*;

import com.github.idemura.cimple.compiler.CompilerException;
import com.github.idemura.cimple.compiler.ErrorConsumer;
import com.github.idemura.cimple.compiler.Location;
import com.github.idemura.cimple.compiler.QualifiedName;
import com.github.idemura.cimple.compiler.ast.AstArrayAccess;
import com.github.idemura.cimple.compiler.ast.AstBlock;
import com.github.idemura.cimple.compiler.ast.AstCall;
import com.github.idemura.cimple.compiler.ast.AstCast;
import com.github.idemura.cimple.compiler.ast.AstDefer;
import com.github.idemura.cimple.compiler.ast.AstEntityRef;
import com.github.idemura.cimple.compiler.ast.AstExpression;
import com.github.idemura.cimple.compiler.ast.AstExpressionStatement;
import com.github.idemura.cimple.compiler.ast.AstFieldAccess;
import com.github.idemura.cimple.compiler.ast.AstFor;
import com.github.idemura.cimple.compiler.ast.AstFunction;
import com.github.idemura.cimple.compiler.ast.AstFunctionHeader;
import com.github.idemura.cimple.compiler.ast.AstFunctionType;
import com.github.idemura.cimple.compiler.ast.AstGoto;
import com.github.idemura.cimple.compiler.ast.AstIf;
import com.github.idemura.cimple.compiler.ast.AstLocal;
import com.github.idemura.cimple.compiler.ast.AstModule;
import com.github.idemura.cimple.compiler.ast.AstNumberLiteral;
import com.github.idemura.cimple.compiler.ast.AstReceiverLookup;
import com.github.idemura.cimple.compiler.ast.AstRecordType;
import com.github.idemura.cimple.compiler.ast.AstReturn;
import com.github.idemura.cimple.compiler.ast.AstStatement;
import com.github.idemura.cimple.compiler.ast.AstStringLiteral;
import com.github.idemura.cimple.compiler.ast.AstType;
import com.github.idemura.cimple.compiler.ast.AstTypeRef;
import com.github.idemura.cimple.compiler.ast.AstUnionType;
import com.github.idemura.cimple.compiler.ast.AstVariable;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;

// Parses token stream, type checks and build the AST.
public class Parser {
  private final Tokenizer tokenizer;
  private final ErrorConsumer errorConsumer;

  // For testing
  public static AstModule parseCode(String code, ErrorConsumer errorConsumer) {
    var tokenizer = new Tokenizer(errorConsumer);
    tokenizer.split(code, null);
    return new Parser(tokenizer, errorConsumer).parse();
  }

  public Parser(Tokenizer tokenizer, ErrorConsumer errorConsumer) {
    this.tokenizer = tokenizer;
    this.errorConsumer = errorConsumer;
  }

  public AstModule parse() {
    return parseModule();
  }

  private AstModule parseModule() {
    var module = new AstModule();
    parseModuleName(module);

    // TODO Parse imports

    while (!tokenizer.done()) {
      switch (keyword(tokenizer.current())) {
        case FUNCTION:
          module.definitions().add(parseFunction());
          break;
        case TYPE:
          module.definitions().add(parseType());
          break;
        case VAR:
          module.definitions().add(parseVariable(true));
          break;
        case CONST:
          module.definitions().add(parseVariable(false));
          break;
        default:
          throw fatalAtCurrentLocation("Invalid module definition");
      }
    }
    return module;
  }

  private AstType parseType() {
    takeKeyword(TYPE);
    return switch (keyword(tokenizer.current())) {
      case RECORD -> parseTypeRecord();
      case FUNCTION -> parseTypeFunction();
      case UNION -> parseTypeUnion();
      default ->
          throw fatalAtCurrentLocation(
              "Invalid type definition: %s allowed", List.of(RECORD, FUNCTION, UNION));
    };
  }

  private AstRecordType parseTypeRecord() {
    var type = new AstRecordType();
    takeKeyword(RECORD);
    type.location(tokenizer.currentLocation());
    type.name(takeIdentifier());
    take(LCURLY);
    var fields = new ImmutableList.Builder<AstVariable>();
    while (!tokenizer.takeIf(RCURLY)) {
      fields.add(parseVariable(keyword(tokenizer.current()) != CONST));
    }
    type.fields(fields.build());
    return type;
  }

  private AstUnionType parseTypeUnion() {
    var type = new AstUnionType();
    takeKeyword(UNION);
    type.location(tokenizer.currentLocation());
    type.name(takeIdentifier());
    take(LCURLY);
    var variants = new ImmutableList.Builder<AstUnionType.Variant>();
    while (!tokenizer.takeIf(RCURLY)) {
      variants.add(parseUnionVariant());
      take(SEMICOLON);
    }
    type.variants(variants.build());
    return type;
  }

  private AstUnionType.Variant parseUnionVariant() {
    var variant = new AstUnionType.Variant();
    variant.location(tokenizer.currentLocation());
    variant.tag(take(IDENTIFIER).value());
    if (tokenizer.takeIf(LPAREN)) {
      variant.valueType(parseTypeRef());
      take(RPAREN);
    }
    return variant;
  }

  private AstFunctionType parseTypeFunction() {
    var type = new AstFunctionType();
    type.header(parseFunctionHeader());
    take(SEMICOLON);
    return type;
  }

  private void parseModuleName(AstModule module) {
    takeKeyword(MODULE);
    module.location(tokenizer.currentLocation());
    module.name(take(IDENTIFIER).value());
    take(SEMICOLON);
  }

  private AstFunction parseFunction() {
    var function = new AstFunction();
    function.header(parseFunctionHeader());
    function.block(parseBlock());
    return function;
  }

  private AstVariable parseVariable(boolean mutable) {
    takeKeyword(mutable ? VAR : CONST);
    var variable = new AstVariable();
    if (mutable) {
      variable.setBit(AstVariable.MUTABLE);
    }
    variable.location(tokenizer.currentLocation());
    variable.name(takeIdentifier());
    if (tokenizer.current().is(IDENTIFIER)) {
      variable.typeRef(parseTypeRef());
    }
    if (tokenizer.takeIf(ASSIGN)) {
      variable.expression(parseExpression());
    }
    take(SEMICOLON);
    return variable;
  }

  private AstLocal parseVariableStatement(boolean mutable) {
    var stmt = new AstLocal();
    stmt.variable(parseVariable(mutable));
    return stmt;
  }

  private AstBlock parseBlock() {
    var block = new AstBlock();
    take(LCURLY);
    while (!tokenizer.takeIf(RCURLY)) {
      block.statements().add(parseStatement());
    }
    return block;
  }

  private AstStatement parseStatement() {
    var keyword = keywordOrNull(tokenizer.current());
    if (keyword == null) {
      return parseExpressionStatement();
    }
    return switch (keyword) {
      case VAR -> parseVariableStatement(true);
      case CONST -> parseVariableStatement(false);
      case RETURN -> parseReturn();
      case IF -> parseIf();
      case FOR -> parseFor();
      case DEFER -> parseDefer();
      case MATCH -> throw new UnsupportedOperationException();
      case GOTO -> parseGoto();
      default ->
          throw fatalAtCurrentLocation("Statement starts with unexpected keyword '%s'", keyword);
    };
  }

  private AstStatement parseReturn() {
    var stmt = new AstReturn();
    stmt.location(takeKeyword(RETURN));
    stmt.expression(parseExpression());
    take(SEMICOLON);
    return stmt;
  }

  private AstStatement parseIf() {
    var stmt = new AstIf();
    stmt.location(takeKeyword(IF));
    var conditions = new ImmutableList.Builder<AstExpression>();
    var thenBlocks = new ImmutableList.Builder<AstBlock>();
    conditions.add(parseExpression());
    thenBlocks.add(parseBlock());
    while (takeKeywordIf(ELSE)) {
      if (takeKeywordIf(IF)) {
        conditions.add(parseExpression());
        thenBlocks.add(parseBlock());
      } else {
        stmt.elseBlock(parseBlock());
        break;
      }
    }
    stmt.conditions(conditions.build());
    stmt.thenBlocks(thenBlocks.build());
    return stmt;
  }

  private AstStatement parseFor() {
    var stmt = new AstFor();
    stmt.location(takeKeyword(FOR));
    if (keywordOrNull(tokenizer.current()) == VAR) {
      stmt.init(parseVariable(true));
    }
    // Condition must be present, even if simple "true".
    stmt.condition(parseExpression());
    if (tokenizer.takeIf(SEMICOLON)) {
      stmt.increment(parseExpression());
    }
    stmt.block(parseBlock());
    return stmt;
  }

  private AstStatement parseGoto() {
    var stmt = new AstGoto();
    stmt.location(takeKeyword(GOTO));
    stmt.label(take(IDENTIFIER).value());
    take(SEMICOLON);
    return stmt;
  }

  private AstStatement parseDefer() {
    var stmt = new AstDefer();
    stmt.location(takeKeyword(DEFER));
    if (tokenizer.current().is(LCURLY)) {
      stmt.block(parseBlock());
    } else {
      var exprStmt = new AstExpressionStatement();
      exprStmt.location(tokenizer.currentLocation());
      exprStmt.expression(parseExpression());
      take(SEMICOLON);
      var block = new AstBlock();
      block.statements().add(exprStmt);
      stmt.block(block);
    }
    return stmt;
  }

  private AstStatement parseExpressionStatement() {
    var stmt = new AstExpressionStatement();
    stmt.location(tokenizer.currentLocation());
    stmt.expression(parseExpression());
    take(SEMICOLON);
    return stmt;
  }

  private AstExpression parseExpression() {
    return parseComparisonChain();
  }

  private AstExpression parseComparisonChain() {
    var expr = parseAdditiveChain();
    if (expr == null) {
      return null;
    }
    while (tokenizer.current().is(CMP_LT) || tokenizer.current().is(CMP_GT)) {
      var operator = tokenizer.take();
      var m = parseAdditiveChain();
      if (m == null) {
        throw errorConsumer.fatalAt(operator.location(), "Expected expression after %s", operator);
      }
      var call = new AstCall();
      call.function(parseOperator(operator));
      call.arguments(ImmutableList.of(expr, m));
      call.location(operator.location());
      expr = call;
    }
    return expr;
  }

  private AstExpression parseAdditiveChain() {
    var expr = parseMultiplicativeChain();
    if (expr == null) {
      return null;
    }
    while (tokenizer.current().is(OP_ADD) || tokenizer.current().is(OP_SUB)) {
      var operator = tokenizer.take();
      var m = parseMultiplicativeChain();
      if (m == null) {
        throw errorConsumer.fatalAt(operator.location(), "Expected expression after %s", operator);
      }
      var call = new AstCall();
      call.function(parseOperator(operator));
      call.arguments(ImmutableList.of(expr, m));
      call.location(operator.location());
      expr = call;
    }
    return expr;
  }

  private AstExpression parseMultiplicativeChain() {
    var expr = parseFieldArrayCallChain();
    if (expr == null) {
      return null;
    }
    while (tokenizer.current().is(OP_MUL)
        || tokenizer.current().is(OP_DIV)
        || tokenizer.current().is(OP_MOD)) {
      var operator = tokenizer.take();
      var m = parseFieldArrayCallChain();
      if (m == null) {
        throw errorConsumer.fatalAt(operator.location(), "Expected expression after %s", operator);
      }
      var call = new AstCall();
      call.function(parseOperator(operator));
      call.arguments(ImmutableList.of(expr, m));
      call.location(operator.location());
      expr = call;
    }
    return expr;
  }

  private AstExpression parseFieldArrayCallChain() {
    var expr = parsePrimary();
    while (true) {
      var current = tokenizer.current();
      if (tokenizer.takeIf(PERIOD)) {
        var fieldAccess = new AstFieldAccess();
        fieldAccess.object(expr);
        fieldAccess.fieldName(take(IDENTIFIER).value());
        expr = fieldAccess;
      } else if (tokenizer.takeIf(COLON)) {
        var receiverLookup = new AstReceiverLookup();
        receiverLookup.receiver(expr);
        receiverLookup.functionName(take(IDENTIFIER).value());
        expr = receiverLookup;
      } else if (tokenizer.takeIf(LBRACKET)) {
        var arrayAccess = new AstArrayAccess();
        arrayAccess.array(expr);
        arrayAccess.index(parseExpression());
        take(RBRACKET);
        expr = arrayAccess;
      } else if (tokenizer.current().is(LPAREN)) {
        var call = new AstCall();
        call.function(expr);
        call.arguments(parseExpressionList());
        expr = call;
      } else {
        break;
      }
      expr.location(current.location());
    }
    return expr;
  }

  // Parses:
  //  - (<expression>)
  //  - [<expression> type <type_ref>]
  //  - <name_ref> | <literal>
  private AstExpression parsePrimary() {
    if (tokenizer.takeIf(LPAREN)) {
      var expr = parseExpression();
      take(RPAREN);
      return expr;
    }
    if (tokenizer.takeIf(LBRACKET)) {
      var expr = new AstCast();
      expr.expression(parseExpression());
      takeKeyword(TYPE);
      expr.typeRef(parseTypeRef());
      take(RBRACKET);
      return expr;
    }
    switch (tokenizer.current().type()) {
      case IDENTIFIER -> {
        var current = tokenizer.take();
        var expr = new AstEntityRef();
        expr.name(parseQualifiedName(current));
        expr.location(current.location());
        return expr;
      }
      case NUMBER -> {
        var current = tokenizer.take();
        var expr = new AstNumberLiteral(current.value());
        expr.location(current.location());
        return expr;
      }
      case STRING -> {
        var current = tokenizer.take();
        var expr = new AstStringLiteral(current.value());
        expr.location(current.location());
        return expr;
      }
      default -> throw fatalAtCurrentLocation("Primary expression expected");
    }
  }

  private List<AstExpression> parseExpressionList() {
    var result = new ImmutableList.Builder<AstExpression>();
    take(LPAREN);
    if (!tokenizer.takeIf(RPAREN)) {
      do {
        result.add(parseExpression());
      } while (expressionListHasNext());
    }
    return result.build();
  }

  private boolean expressionListHasNext() {
    if (tokenizer.takeIf(COMMA)) {
      return true;
    } else if (tokenizer.takeIf(RPAREN)) {
      return false;
    } else {
      throw errorConsumer.fatalAt(
          tokenizer.currentLocation(), "Invalid function call: : or ) expected");
    }
  }

  private AstFunctionHeader parseFunctionHeader() {
    takeKeyword(FUNCTION);
    var header = new AstFunctionHeader();
    var current = take(IDENTIFIER);
    if (tokenizer.takeIf(COLON)) {
      var receiverType = new AstTypeRef();
      receiverType.name(new QualifiedName(current.value()));
      receiverType.location(current.location());
      header.receiverType(receiverType);
      header.location(tokenizer.currentLocation());
      header.name(takeIdentifier());
    } else {
      header.location(current.location());
      header.name(new QualifiedName(current.value()));
    }
    header.parameters(parseParameters());
    if (tokenizer.current().is(IDENTIFIER)) {
      header.resultType(parseTypeRef());
    }
    return header;
  }

  private List<AstVariable> parseParameters() {
    var parameters = new ArrayList<AstVariable>();
    take(LPAREN);
    if (!tokenizer.current().is(RPAREN)) {
      do {
        var variable = new AstVariable();
        variable.location(tokenizer.currentLocation());
        variable.name(takeIdentifier());
        if (tokenizer.current().is(IDENTIFIER)) {
          variable.typeRef(parseTypeRef());
        }
        parameters.add(variable);
      } while (tokenizer.takeIf(COMMA));
    }
    take(RPAREN);
    return parameters;
  }

  private AstTypeRef parseTypeRef() {
    var current = tokenizer.current();
    var ref = new AstTypeRef();
    ref.name(new QualifiedName(current.value()));
    ref.location(current.location());
    tokenizer.step();
    return ref;
  }

  private AstEntityRef parseOperator(Token token) {
    var ref = new AstEntityRef();
    ref.name(QualifiedName.ofBuiltin(token.type().symbolName()));
    ref.location(token.location());
    return ref;
  }

  private CompilerException fatalAtCurrentLocation(String pattern, Object... args) {
    return errorConsumer.fatalAt(tokenizer.currentLocation(), pattern, args);
  }

  private Token take(TokenType type) {
    var current = tokenizer.current();
    if (!current.is(type)) {
      throw fatalAtCurrentLocation("Expected '%s', found '%s'", type, current);
    }
    return tokenizer.take();
  }

  private QualifiedName takeIdentifier() {
    return new QualifiedName(take(IDENTIFIER).value());
  }

  private QualifiedName parseQualifiedName(Token firstIdentifier) {
    if (!tokenizer.takeIf(TILDE)) {
      return new QualifiedName(firstIdentifier.value());
    }
    return new QualifiedName(firstIdentifier.value(), take(IDENTIFIER).value());
  }

  private Location takeKeyword(Keyword keyword) {
    var current = tokenizer.current();
    var kw = keyword(current);
    if (kw != keyword) {
      throw fatalAtCurrentLocation("Expected keyword '%s', found '%s'", keyword, kw);
    }
    return tokenizer.take().location();
  }

  private boolean takeKeywordIf(Keyword keyword) {
    var current = tokenizer.current();
    if (keyword != keywordOrNull(current)) {
      return false;
    }
    tokenizer.step();
    return true;
  }

  private static Keyword keywordOrNull(Token token) {
    if (!token.is(IDENTIFIER)) {
      return null;
    }
    return Keyword.find(token.value());
  }

  private Keyword keyword(Token token) {
    if (!token.is(IDENTIFIER)) {
      throw errorConsumer.fatalAt(token.location(), "Expected keyword, found '%s'", token.type());
    }
    var kw = Keyword.find(token.value());
    if (kw == null) {
      throw errorConsumer.fatalAt(token.location(), "Expected keyword, found '%s'", token.type());
    }
    return kw;
  }
}
