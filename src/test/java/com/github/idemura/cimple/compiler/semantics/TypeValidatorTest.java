package com.github.idemura.cimple.compiler.semantics;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

class TypeValidatorTest extends AbstractSemanticsTest {
  @Test
  void testPointerAndArrayReferencesAreNotRecursive() {
    var code =
        """
        module test;
        type record Node {
          var next Node*;
          var children Node[];
        }
        type union Tree {
          Leaf;
          Nodes(Node[]);
        }
        type function Visitor(v Visitor*);
        """;
    var module = parseCode(code);
    new SemanticAnalyzer(errorConsumer).analyze(List.of(module));
    assertEquals(List.of(), errorConsumer.errors());
  }

  @Test
  void testRecursiveRecordType() {
    var code =
        """
        module test;
        type record Node {
          var next Node;
        }
        """;
    var module = parseCode(code);
    new SemanticAnalyzer(errorConsumer).analyze(List.of(module));
    assertEquals(List.of("Recursive type definition: 'test~Node'"), errorConsumer.errors());
  }

  @Test
  void testRecursiveUnionType() {
    var code =
        """
        module test;
        type union Tree {
          Leaf;
          Nodes(Tree);
        }
        """;
    var module = parseCode(code);
    new SemanticAnalyzer(errorConsumer).analyze(List.of(module));
    assertEquals(List.of("Recursive type definition: 'test~Tree'"), errorConsumer.errors());
  }

  @Test
  void testRecursiveFunctionType() {
    var code =
        """
        module test;
        type function Callback(next Callback);
        """;
    var module = parseCode(code);
    new SemanticAnalyzer(errorConsumer).analyze(List.of(module));
    assertEquals(List.of("Recursive type definition: 'test~Callback'"), errorConsumer.errors());
  }

  @Test
  void testIndirectRecursiveTypes() {
    var code =
        """
        module test;
        type record A {
          var b B;
        }
        type record B {
          var a A;
        }
        """;
    var module = parseCode(code);
    new SemanticAnalyzer(errorConsumer).analyze(List.of(module));
    assertEquals(
        List.of("Recursive type definition: 'test~A'", "Recursive type definition: 'test~B'"),
        errorConsumer.errors());
  }
}
