package io.lang.cimple.compiler.ast;

// Base class for nodes that own a replaceable subtree root.
public abstract sealed class AstHolder extends AstNode permits AstExpressionHolder, AstTypeHolder {}
