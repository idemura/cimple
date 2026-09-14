package io.lang.cimple.compiler;

import com.google.common.collect.ImmutableMap;

public record FunctionMatch(
    AstFunction function, ImmutableMap<AstTypeWildcard, AstType> substitutions) {}
