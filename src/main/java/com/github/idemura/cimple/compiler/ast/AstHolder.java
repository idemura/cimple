package com.github.idemura.cimple.compiler.ast;

// Base class for nodes that own a replaceable subtree root.
public abstract sealed class AstHolder extends AstNode permits AstExpressionHolder, AstTypeHolder {}
