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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

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

  @Test
  public void visit_CssEval_KeyInMapping() {
    // given
    when(cssEval.getKey()).thenReturn("eval");

    // when
    defCollectorVisitor.visit(cssEval, null);

    Map<String, String> mapping = defCollectorVisitor.getDefMapping();

    // then
    assertTrue(mapping.containsKey("eval"));
    assertEquals("EVAL", mapping.get("eval"));
  }

  @Test
  public void visit_CssDef_KeyInMapping() {
    // given
    when(cssDef.getKey()).thenReturn("def");

    // when
    defCollectorVisitor.visit(cssDef, null);

    Map<String, String> mapping = defCollectorVisitor.getDefMapping();

    // then
    assertTrue(mapping.containsKey("def"));
    assertEquals("DEF", mapping.get("def"));
  }

  @Test
  public void visit_CssUrl_KeyInMapping() {
    // given
    when(cssUrl.getKey()).thenReturn("url");

    // when
    defCollectorVisitor.visit(cssUrl, null);

    Map<String, String> mapping = defCollectorVisitor.getDefMapping();

    // then
    assertTrue(mapping.containsKey("url"));
    assertEquals("URL", mapping.get("url"));
  }


  @Test
  public void visit_ConstantInCamelCase_UpperCaseKeyInMapping() {
    // given
    when(cssDef.getKey()).thenReturn("myConstantName");

    // when
    defCollectorVisitor.visit(cssDef, null);

    // then
    Map<String, String> mapping = defCollectorVisitor.getDefMapping();

    // then
    assertTrue(mapping.containsKey("myConstantName"));
    assertEquals("MY_CONSTANT_NAME", mapping.get("myConstantName"));
  }

  @Test
  public void visit_ConstantInUpperCase_UpperCaseKeyInMapping() {
    // given
    when(cssDef.getKey()).thenReturn("MY_UPPERCASE_CONSTANT");

    // when
    defCollectorVisitor.visit(cssDef, null);

    Map<String, String> mapping = defCollectorVisitor.getDefMapping();

    // then
    assertTrue(mapping.containsKey("MY_UPPERCASE_CONSTANT"));
    assertEquals("MY_UPPERCASE_CONSTANT", mapping.get("MY_UPPERCASE_CONSTANT"));
  }
}
