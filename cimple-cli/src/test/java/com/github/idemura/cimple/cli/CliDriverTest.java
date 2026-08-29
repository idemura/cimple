package com.github.idemura.cimple.cli;

import static org.junit.jupiter.api.Assertions.*;

import com.github.idemura.cimple.compiler.codegen.c.CStandard;
import org.junit.jupiter.api.Test;

class CliDriverTest {
  @Test
  void testParseCCodeGeneratorParams() {
    var cli = new CliDriver();
    cli.parseCmdLine(
        new String[] {
          "--codegen", "c",
          "--c_standard", "C17",
          "--c_mangle_module_name", "false",
          "--c_output_preamble", "false",
          "test.ci"
        });
    assertEquals("c", cli.codeGen);
    assertEquals(CStandard.C17, cli.cStandard);
    assertFalse(cli.cMangleModuleName);
    assertFalse(cli.cOutputPreamble);
    assertEquals("test.ci", cli.files.get(0));
  }
}
