package io.lang.cimple.compiler;

import static io.lang.cimple.compiler.Keyword.BREAK;
import static io.lang.cimple.compiler.Keyword.CONST;
import static io.lang.cimple.compiler.Keyword.DEFER;
import static io.lang.cimple.compiler.Keyword.DELETE;
import static io.lang.cimple.compiler.Keyword.ELSE;
import static io.lang.cimple.compiler.Keyword.ENUM;
import static io.lang.cimple.compiler.Keyword.FOR;
import static io.lang.cimple.compiler.Keyword.FUNCTION;
import static io.lang.cimple.compiler.Keyword.IF;
import static io.lang.cimple.compiler.Keyword.INTERFACE;
import static io.lang.cimple.compiler.Keyword.MODULE;
import static io.lang.cimple.compiler.Keyword.NEW;
import static io.lang.cimple.compiler.Keyword.RETURN;
import static io.lang.cimple.compiler.Keyword.STRUCT;
import static io.lang.cimple.compiler.Keyword.TYPE;
import static io.lang.cimple.compiler.Keyword.UNION;
import static io.lang.cimple.compiler.Keyword.VAR;
import static io.lang.cimple.compiler.TokenType.ASSIGN;
import static io.lang.cimple.compiler.TokenType.BANG;
import static io.lang.cimple.compiler.TokenType.CMP_EQ;
import static io.lang.cimple.compiler.TokenType.CMP_GE;
import static io.lang.cimple.compiler.TokenType.CMP_GT;
import static io.lang.cimple.compiler.TokenType.CMP_LE;
import static io.lang.cimple.compiler.TokenType.CMP_LT;
import static io.lang.cimple.compiler.TokenType.CMP_NE;
import static io.lang.cimple.compiler.TokenType.COMMA;
import static io.lang.cimple.compiler.TokenType.IDENTIFIER;
import static io.lang.cimple.compiler.TokenType.LBRACKET;
import static io.lang.cimple.compiler.TokenType.LCURLY;
import static io.lang.cimple.compiler.TokenType.LPAREN;
import static io.lang.cimple.compiler.TokenType.MINUS;
import static io.lang.cimple.compiler.TokenType.MINUS_ASSIGN;
import static io.lang.cimple.compiler.TokenType.PERCENT;
import static io.lang.cimple.compiler.TokenType.PERCENT_ASSIGN;
import static io.lang.cimple.compiler.TokenType.PERIOD;
import static io.lang.cimple.compiler.TokenType.PLUS;
import static io.lang.cimple.compiler.TokenType.PLUS_ASSIGN;
import static io.lang.cimple.compiler.TokenType.RBRACKET;
import static io.lang.cimple.compiler.TokenType.RCURLY;
import static io.lang.cimple.compiler.TokenType.RPAREN;
import static io.lang.cimple.compiler.TokenType.SEMICOLON;
import static io.lang.cimple.compiler.TokenType.SLASH;
import static io.lang.cimple.compiler.TokenType.SLASH_ASSIGN;
import static io.lang.cimple.compiler.TokenType.STAR;
import static io.lang.cimple.compiler.TokenType.STAR_ASSIGN;
import static io.lang.cimple.compiler.TokenType.TILDE;

import com.google.common.collect.ImmutableList;
import java.util.List;

// Parses the token stream and builds the AST. Semantic analysis runs later.
public class Parser {
  private final Tokenizer tokenizer;
  private final ErrorConsumer errorConsumer;

  // Test helper for parsing a single in-memory source string.
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
    var builder = new AstModuleBuilder();
    takeKeyword(MODULE);
    builder.name(parseName());
    take(SEMICOLON);

    // TODO: Parse imports when the module system is implemented.

    while (!tokenizer.done()) {
      switch (currentKeyword()) {
        case FUNCTION:
          builder.definition(parseFunction(false));
          break;
        case TYPE:
          builder.definition(parseType());
          break;
        case VAR:
        case CONST:
          builder.definition(parseVariable());
          break;
        default:
          throw fatalAtCurrentLocation("Invalid module definition");
      }
    }
    return builder.build();
  }

  private AstType parseType() {
    takeKeyword(TYPE);
    if (isKeyword(STRUCT)) {
      return parseTypeStruct();
    }
    if (isKeyword(UNION)) {
      return parseTypeUnion();
    }
    if (isKeyword(ENUM)) {
      return parseTypeEnum();
    }
    if (isKeyword(FUNCTION)) {
      return parseTypeFunction();
    }
    if (isKeyword(INTERFACE)) {
      return parseTypeInterface();
    }
    throw fatalAtCurrentLocation(
        "Invalid type definition: one of %s expected",
        List.of(INTERFACE, STRUCT, UNION, ENUM, FUNCTION));
  }

  private AstStructType parseTypeStruct() {
    var builder = new AstStructTypeBuilder();
    takeKeyword(STRUCT);
    builder.name(parseName());
    take(LCURLY);
    while (!tokenizer.takeIf(RCURLY)) {
      builder.field(parseVariable());
    }
    return builder.build();
  }

  private AstUnionType parseTypeUnion() {
    var builder = new AstUnionTypeBuilder();
    takeKeyword(UNION);
    builder.name(parseName());
    take(LCURLY);
    while (!tokenizer.takeIf(RCURLY)) {
      var location = tokenizer.currentLocation();
      var tag = parseName();
      AstType valueType;
      if (tokenizer.takeIf(LPAREN)) {
        valueType = parseTypeRef();
        take(RPAREN);
      } else {
        valueType = null;
      }
      builder.variant(new AstUnionVariant(tag, valueType));
      take(SEMICOLON);
    }
    return builder.build();
  }

  private AstEnumType parseTypeEnum() {
    var builder = new AstEnumTypeBuilder();
    takeKeyword(ENUM);
    builder.name(parseName());
    if (tokenizer.takeIf(LPAREN)) {
      builder.baseType(parseTypeRef());
      take(RPAREN);
    }
    take(LCURLY);
    while (!tokenizer.takeIf(RCURLY)) {
      var tag = parseName();
      AstExpression expression;
      if (tokenizer.takeIf(LPAREN)) {
        expression = parseExpression();
        take(RPAREN);
      } else {
        expression = null;
      }
      builder.variant(new AstEnumVariant(tag, expression));
      take(SEMICOLON);
    }
    return builder.build();
  }

  private AstFunctionType parseTypeFunction() {
    return new AstFunctionType(parseFunction(true));
  }

  private AstInterfaceType parseTypeInterface() {
    var builder = new AstInterfaceTypeBuilder();
    takeKeyword(INTERFACE);
    builder.name(parseName());
    take(LCURLY);
    while (!tokenizer.takeIf(RCURLY)) {
      builder.function(parseFunction(true));
    }
    return builder.build();
  }

  private AstFunction parseFunction(boolean declarationOnly) {
    var builder = new AstFunctionBuilder();
    takeKeyword(FUNCTION);
    builder.name(parseName());
    parseParameters(builder);
    if (tokenizer.current().is(IDENTIFIER)) {
      builder.resultType(parseTypeRef());
    }
    if (declarationOnly) {
      take(SEMICOLON);
    } else {
      if (!tokenizer.takeIf(SEMICOLON)) {
        builder.block(parseBlock());
      }
    }
    return builder.build();
  }

  private AstVariable parseVariable() {
    var builder = new AstVariableBuilder();
    var mutable = isKeyword(VAR);
    if (mutable) {
      builder.flag(AstVariable.MUTABLE);
    }
    takeKeyword(mutable ? VAR : CONST);
    builder.name(parseName());
    if (tokenizer.current().is(IDENTIFIER)) {
      builder.type(parseTypeRef());
    }
    if (tokenizer.takeIf(ASSIGN)) {
      builder.expression(parseExpression());
    }
    take(SEMICOLON);
    return builder.build();
  }

  private AstLocal parseVariableStatement() {
    return new AstLocal(parseVariable());
  }

  private AstBlock parseBlock() {
    var builder = new AstBlockBuilder();
    take(LCURLY);
    while (!tokenizer.takeIf(RCURLY)) {
      builder.statement(parseStatement());
    }
    return builder.build();
  }

  private AstStatement parseStatement() {
    var keyword = Keyword.fromString(tokenizer.current().value());
    if (keyword == null) {
      return parseExpressionStatement();
    }
    return switch (keyword) {
      case VAR, CONST -> parseVariableStatement();
      case RETURN -> endWithSemicolon(parseReturn());
      case BREAK -> endWithSemicolon(parseBreak());
      case DELETE -> endWithSemicolon(parseDelete());
      case IF -> parseIf();
      case FOR -> parseFor();
      case DEFER -> parseDefer();
      case MATCH -> throw new UnsupportedOperationException();
      default ->
          throw fatalAtCurrentLocation("Statement starts with unexpected keyword '%s'", keyword);
    };
  }

  private AstStatement endWithSemicolon(AstStatement statement) {
    take(SEMICOLON);
    return statement;
  }

  private AstStatement parseReturn() {
    return new AstReturn(takeKeyword(RETURN), parseExpression());
  }

  private AstStatement parseBreak() {
    return new AstBreak(takeKeyword(BREAK));
  }

  private AstStatement parseDelete() {
    return new AstDelete(takeKeyword(DELETE), parseExpression());
  }

  private AstStatement parseIf() {
    var builder = new AstIfBuilder();
    builder.location(takeKeyword(IF));
    builder.branch(parseExpression(), parseBlock());
    while (isKeyword(ELSE)) {
      tokenizer.step();
      if (isKeyword(IF)) {
        tokenizer.step();
        builder.branch(parseExpression(), parseBlock());
      } else {
        builder.elseBlock(parseBlock());
        break;
      }
    }
    return builder.build();
  }

  private AstStatement parseFor() {
    var builder = new AstForBuilder();
    builder.location(takeKeyword(FOR));
    if (currentKeyword() == VAR) {
      builder.init(parseVariableStatement());
    }
    // The loop condition is required, even for an infinite loop such as `for true ...`.
    builder.condition(parseExpression());
    if (tokenizer.takeIf(SEMICOLON)) {
      builder.increment(parseExpression());
    }
    return builder.build();
  }

  private AstStatement parseDefer() {
    var location = takeKeyword(DEFER);
    AstBlock block;
    if (tokenizer.current().is(LCURLY)) {
      block = parseBlock();
    } else {
      var builder = new AstBlockBuilder();
      builder.statement(parseExpressionStatement());
      block = builder.build();
    }
    return new AstDefer(location, block);
  }

  private AstStatement parseExpressionStatement() {
    return endWithSemicolon(new AstExpressionStatement(parseExpression()));
  }

  private AstExpression parseExpression() {
    return parseAssignment();
  }

  private AstExpression parseAssignment() {
    var target = parseComparisonChain();
    if (target == null) {
      return null;
    }
    var current = tokenizer.current();
    if (tokenizer.takeIf(ASSIGN)) {
      var value = parseAssignment();
      if (value == null) {
        throw errorConsumer.fatalAt(current.location(), "Expected expression after %s", current);
      }
      var expr = new AstAssign(current.location());
      expr.target(target);
      expr.value(value);
      return expr;
    }
    if (isCompoundAssignment(current.type())) {
      tokenizer.step();
      var value = parseAssignment();
      if (value == null) {
        throw errorConsumer.fatalAt(current.location(), "Expected expression after %s", current);
      }
      var expr = new AstCompoundAssign(current.location());
      expr.target(target);
      expr.operation(parseCompoundAssignmentOperator(current));
      expr.value(value);
      return expr;
    }
    return target;
  }

  private static boolean isCompoundAssignment(TokenType type) {
    return type == PLUS_ASSIGN
        || type == MINUS_ASSIGN
        || type == STAR_ASSIGN
        || type == SLASH_ASSIGN
        || type == PERCENT_ASSIGN;
  }

  private AstFunctionRef parseCompoundAssignmentOperator(Token token) {
    var operator =
        switch (token.type()) {
          case PLUS_ASSIGN -> PLUS;
          case MINUS_ASSIGN -> MINUS;
          case STAR_ASSIGN -> STAR;
          case SLASH_ASSIGN -> SLASH;
          case PERCENT_ASSIGN -> PERCENT;
          default -> throw new IllegalArgumentException("Not a compound assignment: " + token);
        };
    return parseOperator(new Token(operator, null, token.location()));
  }

  private AstExpression parseComparisonChain() {
    var expr = parseAdditiveChain();
    if (expr == null) {
      return null;
    }
    while (isComparisonOperator(tokenizer.current().type())) {
      var operator = tokenizer.take();
      var m = parseAdditiveChain();
      if (m == null) {
        throw errorConsumer.fatalAt(operator.location(), "Expected expression after %s", operator);
      }
      expr = new AstCall(parseOperator(operator), ImmutableList.of(expr, m));
    }
    return expr;
  }

  private static boolean isComparisonOperator(TokenType type) {
    return type == CMP_EQ
        || type == CMP_NE
        || type == CMP_LT
        || type == CMP_GT
        || type == CMP_LE
        || type == CMP_GE;
  }

  private AstExpression parseAdditiveChain() {
    var expr = parseMultiplicativeChain();
    if (expr == null) {
      return null;
    }
    while (tokenizer.current().is(PLUS) || tokenizer.current().is(MINUS)) {
      var operator = tokenizer.take();
      var m = parseMultiplicativeChain();
      if (m == null) {
        throw errorConsumer.fatalAt(operator.location(), "Expected expression after %s", operator);
      }
      expr = new AstCall(parseOperator(operator), ImmutableList.of(expr, m));
    }
    return expr;
  }

  private AstExpression parseMultiplicativeChain() {
    var expr = parseAccessorChainCall();
    if (expr == null) {
      return null;
    }
    while (tokenizer.current().is(STAR)
        || tokenizer.current().is(SLASH)
        || tokenizer.current().is(PERCENT)) {
      var operator = tokenizer.take();
      var m = parseAccessorChainCall();
      if (m == null) {
        throw errorConsumer.fatalAt(operator.location(), "Expected expression after %s", operator);
      }
      expr = new AstCall(parseOperator(operator), ImmutableList.of(expr, m));
    }
    return expr;
  }

  private AstExpression parseAccessorChainCall() {
    var expr = isKeyword(NEW) ? parseNew() : parsePrimary();
    while (true) {
      var current = tokenizer.current();
      if (tokenizer.takeIf(PERIOD)) {
        expr = new AstFieldAccess(current.location(), expr, take(IDENTIFIER).value());
      } else if (tokenizer.takeIf(LBRACKET)) {
        expr = new AstArrayAccess(current.location(), expr, parseExpression());
        take(RBRACKET);
      } else if (tokenizer.takeIf(BANG)) {
        expr = new AstFunctionPointerCall(current.location(), expr, parseExpressionList());
      } else if (tokenizer.current().is(LPAREN)) {
        // TODO Just entity ref and check lookup function when resolve.
        if (expr instanceof AstVariableRef variable) {
          var function = new AstFunctionRef(variable.name());
          expr = new AstCall(function, parseExpressionList());
        } else {
          throw errorConsumer.fatalAt(current.location(), "Expected function name before '('");
        }
      } else {
        break;
      }
    }
    return expr;
  }

  private AstNew parseNew() {
    return new AstNew(takeKeyword(NEW), parseTypeRef(), parseExpressionList());
  }

  // Parses one primary expression:
  //   - (<expression>)
  //   - (<expression> type <type-ref>)
  //   - <identifier> or <module>~<identifier>
  //   - <literal>
  private AstExpression parsePrimary() {
    if (tokenizer.current().is(LPAREN)) {
      var expression = parseExpression();
      if (isKeyword(TYPE)) {
        expression = new AstCast(takeKeyword(TYPE), expression, parseTypeRef());
      }
      take(RPAREN);
      return expression;
    }
    switch (tokenizer.current().type()) {
      case IDENTIFIER -> {
        var expr = new AstVariableRef(parseQualifiedName());
        return expr;
      }
      case NUMBER -> {
        var current = tokenizer.take();
        return new AstNumberLiteral(current.location(), current.value());
      }
      case STRING -> {
        var current = tokenizer.take();
        return new AstStringLiteral(current.location(), current.value());
      }
      default -> throw fatalAtCurrentLocation("Primary expression expected");
    }
  }

  private ImmutableList<AstExpression> parseExpressionList() {
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
          tokenizer.currentLocation(), "Invalid function call: , or ) expected");
    }
  }

  private void parseParameters(AstFunctionBuilder builder) {
    take(LPAREN);
    if (!tokenizer.current().is(RPAREN)) {
      do {
        var name = parseName();
        var type = parseTypeRef();
        builder.parameter(new AstVariable(name, type, null));
      } while (tokenizer.takeIf(COMMA));
    }
    take(RPAREN);
  }

  private AstType parseTypeRef() {
    AstType type = new AstTypeRef(parseName());
    tokenizer.step();
    while (true) {
      if (tokenizer.takeIf(STAR)) {
        type = new AstPointerType(type);
      } else if (tokenizer.current().is(LBRACKET)) {
        tokenizer.step();
        take(RBRACKET);
        type = new AstArrayType(type);
      } else {
        break;
      }
    }
    return type;
  }

  private Identifier parseName() {
    var current = take(IDENTIFIER);
    return new Identifier(current.value()).location(current.location());
  }

  private Identifier parseQualifiedName() {
    var first = take(IDENTIFIER);
    if (tokenizer.takeIf(TILDE)) {
      return new Identifier(take(IDENTIFIER).value())
          .module(first.value())
          .location(first.location());
    } else {
      return new Identifier(first.value()).location(first.location());
    }
  }

  private AstFunctionRef parseOperator(Token token) {
    var name = new Identifier(token.type().symbol()).location(token.location()).builtin();
    return new AstFunctionRef(name);
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

  private Location takeKeyword(Keyword keyword) {
    var currentKeyword = currentKeyword();
    if (currentKeyword != keyword) {
      throw fatalAtCurrentLocation("Expected keyword '%s', found '%s'", keyword, currentKeyword);
    }
    return tokenizer.take().location();
  }

  private boolean isKeyword(Keyword keyword) {
    var token = tokenizer.current();
    return keyword == Keyword.fromString(token.value());
  }

  private Keyword currentKeyword() {
    var token = tokenizer.current();
    var keyword = Keyword.fromString(token.value());
    if (keyword == null) {
      throw errorConsumer.fatalAt(token.location(), "Expected keyword, found '%s'", token.type());
    }
    return keyword;
  }
}
