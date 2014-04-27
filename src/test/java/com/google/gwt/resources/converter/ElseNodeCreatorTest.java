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


import com.google.gwt.resources.css.ast.CssIf;
import com.google.gwt.resources.css.ast.CssNode;
import com.google.gwt.resources.css.ast.CssRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ElseNodeCreatorTest {
  private ElseNodeCreator elseNodeCreator;
  @Mock
  private CssIf cssIf;
  private List<CssNode> elseNodes;

  @Before
  public void setUp() {
    elseNodeCreator = new ElseNodeCreator();
    elseNodes = new ArrayList<CssNode>();
    when(cssIf.getElseNodes()).thenReturn(elseNodes);
  }

  @After
  public void tearDown() {
    elseNodeCreator = null;
    cssIf = null;
    elseNodes = null;
  }

  @Test
  public void testIfElse() {
    CssRule elseRule = new CssRule();
    elseNodes.add(elseRule);

    elseNodeCreator.visit(cssIf, null);

    assertEquals(1, elseNodes.size());
    assertTrue(elseNodes.get(0) instanceof CssElse);
    assertEquals(1, ((CssElse) elseNodes.get(0)).getNodes().size());
    assertTrue(((CssElse) elseNodes.get(0)).getNodes().contains(elseRule));
  }

  @Test
  public void testIfElseWithSeveralRul() {
    CssRule elseRule1 = new CssRule();
    CssRule elseRule2 = new CssRule();
    CssRule elseRule3 = new CssRule();
    elseNodes.add(elseRule1);
    elseNodes.add(elseRule2);
    elseNodes.add(elseRule3);

    elseNodeCreator.visit(cssIf, null);

    assertEquals(1, elseNodes.size());
    assertTrue(elseNodes.get(0) instanceof CssElse);

    CssElse newElseNode = (CssElse) elseNodes.get(0);
    assertEquals(3, newElseNode.getNodes().size());
    assertTrue(newElseNode.getNodes().contains(elseRule1));
    assertTrue(newElseNode.getNodes().contains(elseRule2));
    assertTrue(newElseNode.getNodes().contains(elseRule3));
  }

  @Test
  public void testIfElif() {
    CssIf elifNode = new CssIf();
    elseNodes.add(elifNode);

    elseNodeCreator.visit(cssIf, null);

    assertEquals(1, elseNodes.size());
    assertTrue(elseNodes.get(0) instanceof CssElIf);
    assertEquals(1, ((CssElse) elseNodes.get(0)).getNodes().size());
    assertTrue(((CssElse) elseNodes.get(0)).getNodes().contains(elseRule));
  }
}
