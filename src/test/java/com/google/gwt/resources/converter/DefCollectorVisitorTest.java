/*
 * Copyright 2013 Julien Dramaix.
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

import com.google.gwt.resources.css.ast.CssDef;
import com.google.gwt.resources.css.ast.CssEval;
import com.google.gwt.resources.css.ast.CssUrl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DefCollectorVisitorTest {
  private DefCollectorVisitor defCollectorVisitor;
  @Mock
  private CssEval cssEval;
  @Mock
  private CssDef cssDef;
  @Mock
  private CssUrl cssUrl;

  @Before
  public void setUp() {
    defCollectorVisitor = new DefCollectorVisitor();
  }

  @After
  public void tearDown() {
    defCollectorVisitor = null;
    cssDef = null;
    cssEval = null;
    cssUrl = null;
  }

  @Test
  public void testVisitCssEval() {
    when(cssEval.getKey()).thenReturn("eval");

    defCollectorVisitor.visit(cssEval, null);

    Map<String, String> mapping = defCollectorVisitor.getDefMapping();

    assertTrue(mapping.containsKey("eval"));
    assertEquals("EVAL", mapping.get("eval"));
  }

  @Test
  public void testVisitCssDef() {
    when(cssDef.getKey()).thenReturn("def");

    defCollectorVisitor.visit(cssDef, null);

    Map<String, String> mapping = defCollectorVisitor.getDefMapping();

    assertTrue(mapping.containsKey("def"));
    assertEquals("DEF", mapping.get("def"));
  }

  @Test
  public void testVisitCssUrl() {
    when(cssUrl.getKey()).thenReturn("url");

    defCollectorVisitor.visit(cssUrl, null);

    Map<String, String> mapping = defCollectorVisitor.getDefMapping();

    assertTrue(mapping.containsKey("url"));
    assertEquals("URL", mapping.get("url"));
  }


  @Test
  public void testDefCamelCase() {
    when(cssDef.getKey()).thenReturn("myConstantName");

    defCollectorVisitor.visit(cssDef, null);

    Map<String, String> mapping = defCollectorVisitor.getDefMapping();

    assertTrue(mapping.containsKey("myConstantName"));
    assertEquals("MY_CONSTANT_NAME", mapping.get("myConstantName"));
  }

  @Test
  public void testDefUpperCase() {
    when(cssDef.getKey()).thenReturn("MY_UPPERCASE_CONSTANT");

    defCollectorVisitor.visit(cssDef, null);

    Map<String, String> mapping = defCollectorVisitor.getDefMapping();

    assertTrue(mapping.containsKey("MY_UPPERCASE_CONSTANT"));
    assertEquals("MY_UPPERCASE_CONSTANT", mapping.get("MY_UPPERCASE_CONSTANT"));
  }
}
