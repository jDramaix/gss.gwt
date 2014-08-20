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
  public void testInlineBlockCssEscaping() throws IOException {
    assertFileContentEqualsAfterConversion("inline-block.css", "inline-block.gss");
  }

  @Test
  public void testMultipleDeclarationOfSameProperty() throws IOException {
    assertFileContentEqualsAfterConversion("multiple_declarations.css",
        "multiple_declarations.gss");
  }

  @Test
  public void testCssConditional()  throws IOException {
    assertFileContentEqualsAfterConversion("conditional.css", "conditional.gss");
  }

  private void assertFileContentEqualsAfterConversion(String inputCssFile, String expectedGssFile)
      throws IOException {
    URL resource = Css2GssTest.class.getResource(inputCssFile);
    InputStream stream = Css2GssTest.class.getResourceAsStream(expectedGssFile);
    String convertedGss = new Css2Gss(resource, new PrintWriter(System.err)).toGss();
    String gss = IOUtils.toString(stream, "UTF-8");
    Assert.assertEquals(gss, convertedGss);
  }
}
