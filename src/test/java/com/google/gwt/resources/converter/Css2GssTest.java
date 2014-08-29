/*
 * Copyright 2014 Daniel Kurka.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.gwt.resources.converter;


import com.google.gwt.core.ext.UnableToCompleteException;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;

/**
 * Integration tests for Css2Gss
 */
public class Css2GssTest {

  @Test
  public void testInlineBlockCssEscaping() throws Exception {
    assertFileContentEqualsAfterConversion("inline-block.css", "inline-block.gss");
  }

  @Test
  public void testMultipleDeclarationOfSameProperty() throws Exception {
    assertFileContentEqualsAfterConversion("multiple_declarations.css",
        "multiple_declarations.gss");
  }

  @Test
  public void testCssConditional() throws Exception {
    assertFileContentEqualsAfterConversion("conditional.css", "conditional.gss");
  }

  @Test
  public void testLenientFlag() throws Exception {
    assertFileContentEqualsAfterConversion("badRule.css", "badRule.gss", true);
  }

  @Test
  public void testExternalMissingComma() throws Exception {
    assertFileContentEqualsAfterConversion("external-bug.css", "external-bug.gss", true);
  }

  @Test
  public void testSprite() throws Exception {
    assertFileContentEqualsAfterConversion("sprite.css", "sprite.gss");
  }

  @Test
  public void testFontFamily() throws Exception {
    assertFileContentEqualsAfterConversion("font-bug.css", "font-bug.gss");
  }

  @Test
  public void testExternalBug() throws Exception {
    assertFileContentEqualsAfterConversion("external-bug.css", "external-bug.gss", true);
  }

  @Test
  public void testUndefinedConstant() throws Exception {
    assertFileContentEqualsAfterConversion(
        "undefined-constants.css", "undefined-constants.gss", true);
  }

  @Test
  public void testRemoveExternalEscaping() throws Exception {
    assertFileContentEqualsAfterConversion(
        "external-escaping.css", "external-escaping.gss");
  }

  @Test
  public void testNestedConditional() throws Exception {
    assertFileContentEqualsAfterConversion(
        "nestedElseIf.css", "nestedElseIf.gss");
  }

  @Test
  public void testConstants() throws Exception {
    assertFileContentEqualsAfterConversion(
        "constants.css", "constants.gss");
  }

  @Test
  public void testInvalidConstantName() throws IOException, UnableToCompleteException {
    assertFileContentEqualsAfterConversion(
        "invalidConstantName.css", "invalidConstantName.gss", true);
  }

  private void assertFileContentEqualsAfterConversion(String inputCssFile, String expectedGssFile)
      throws IOException, UnableToCompleteException {
    assertFileContentEqualsAfterConversion(inputCssFile, expectedGssFile, false);
  }

  private void assertFileContentEqualsAfterConversion(String inputCssFile,
      String expectedGssFile, boolean lenient) throws IOException, UnableToCompleteException {
    URL resource = Css2GssTest.class.getResource(inputCssFile);
    InputStream stream = Css2GssTest.class.getResourceAsStream(expectedGssFile);
    String convertedGss = new Css2Gss(resource, lenient).toGss();
    String gss = IOUtils.toString(stream, "UTF-8");
    Assert.assertEquals(gss, convertedGss);
  }
}
