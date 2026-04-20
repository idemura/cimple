package com.github.idemura.cimple.compiler;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

class FunctionOverloadTest {
  private final TypeRef a = TypeRef.ofName("a");
  private final TypeRef b = TypeRef.ofName("b");
  private final TypeRef x = TypeRef.ofName("x");
  private final TypeRef y = TypeRef.ofName("y");
  private final TypeRef z = TypeRef.ofName("z");
  private final TypeRef r = TypeRef.ofName("r");
  private final TypeRef p = TypeRef.ofName("p");

  static AstFunction func(TypeRef resultType, TypeRef... params) {
    return new AstFunction(
        null,
        "f",
        resultType,
        List.of(params).stream().map(t -> new VariableDef(null, "_", t)).toList(),
        null);
  }

  @Test
  void testSingle() {
    FunctionOverload.create(List.of(func(r, a, b)));
  }

  @Test
  void testOverloadsOfDifferentSize() {
    assertThrows(
        CompilerException.class, () -> FunctionOverload.create(List.of(func(r, a), func(r, a, b))));
  }

  @Test
  void testOverloadsOf1() {
    FunctionOverload.create(List.of(func(r, a), func(r, b)));
    assertThrows(
        CompilerException.class, () -> FunctionOverload.create(List.of(func(r, a), func(r, a))));
  }

  @Test
  void testOverloadsOf2() {
    FunctionOverload.create(List.of(func(r, a, x), func(r, a, y)));
    assertThrows(
        CompilerException.class,
        () -> FunctionOverload.create(List.of(func(r, a, x), func(r, b, y))));
    assertThrows(
        CompilerException.class,
        () -> FunctionOverload.create(List.of(func(r, a, x), func(r, b, x))));
  }

  @Test
  void testOverloadsOf() {
    FunctionOverload.create(List.of(func(r, a, x, x), func(r, a, y, y)));
    FunctionOverload.create(List.of(func(r, a, x, x), func(r, a, y, y), func(r, a, z, z)));
    FunctionOverload.create(List.of(func(r, a, b, x), func(r, a, b, y)));
    FunctionOverload.create(List.of(func(r, a, b, x), func(r, a, b, y), func(r, a, b, z)));
  }

  @Test
  void testResultType() {
    FunctionOverload.create(List.of(func(r, a, x), func(r, a, y), func(r, a, z)));
    FunctionOverload.create(List.of(func(x, a, x), func(y, a, y), func(z, a, z)));

    assertThrows(
        CompilerException.class,
        () -> FunctionOverload.create(List.of(func(r, a, x), func(p, a, y))));
    assertThrows(
        CompilerException.class,
        () -> FunctionOverload.create(List.of(func(r, a, x), func(r, a, y), func(p, a, z))));
    assertThrows(
        CompilerException.class,
        () -> FunctionOverload.create(List.of(func(x, a, x), func(y, a, y), func(r, a, z))));
  }

  @Test
  void testResolveOverloadOf1() {
    var overload = FunctionOverload.create(List.of(func(r, a, b)));

    assertEquals(func(r, a, b), overload.resolve(List.of(a, b)));

    assertThrows(CompilerException.class, () -> overload.resolve(List.of(a)));
    assertThrows(CompilerException.class, () -> overload.resolve(List.of(a, a)));
    assertThrows(CompilerException.class, () -> overload.resolve(List.of(b, b)));
    assertThrows(CompilerException.class, () -> overload.resolve(List.of(a, x)));
    assertThrows(CompilerException.class, () -> overload.resolve(List.of(a, b, x)));
  }

  @Test
  void testResolveOverloadOf2() {
    var overload = FunctionOverload.create(List.of(func(x, a, x), func(y, a, y)));

    assertEquals(func(x, a, x), overload.resolve(List.of(a, x)));
    assertEquals(func(y, a, y), overload.resolve(List.of(a, y)));

    assertThrows(CompilerException.class, () -> overload.resolve(List.of(a)));
    assertThrows(CompilerException.class, () -> overload.resolve(List.of(a, b)));
    assertThrows(CompilerException.class, () -> overload.resolve(List.of(a, b, x)));
    assertThrows(CompilerException.class, () -> overload.resolve(List.of(b, x)));
    assertThrows(CompilerException.class, () -> overload.resolve(List.of(b, y)));
  }

  @Test
  void testResolveOverloadOf2_4Args() {
    var overload = FunctionOverload.create(List.of(func(r, a, b, x, x), func(r, a, b, y, y)));

    AstFunction f;
    f = overload.resolve(List.of(a, b, x, x));
    assertEquals(func(r, a, b, x, x), f);
    f = overload.resolve(List.of(a, b, y, y));
    assertEquals(func(r, a, b, y, y), f);
  }
}
