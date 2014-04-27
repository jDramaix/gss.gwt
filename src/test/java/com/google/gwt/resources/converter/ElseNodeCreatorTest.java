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


import com.google.common.collect.Lists;
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
import static org.mockito.Mockito.mock;
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
  public void testIf() {
    elseNodeCreator.visit(cssIf, null);

    assertEquals(0, elseNodes.size());
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
    CssIf elifNode = mockCssIf(0);

    elseNodes.add(elifNode);

    elseNodeCreator.visit(cssIf, null);

    assertEquals(1, elseNodes.size());
    assertTrue(elseNodes.get(0) instanceof CssElIf);

    verify((CssElIf) elseNodes.get(0), elifNode);
  }

  @Test
  public void testIfElifElse() {
    CssIf elifNode = mockCssIf(0);
    CssNode elseRule = new CssRule();
    when(elifNode.getElseNodes()).thenReturn(Lists.newArrayList(elseRule));


    elseNodes.add(elifNode);

    elseNodeCreator.visit(cssIf, null);

    assertEquals(2, elseNodes.size());
    assertTrue(elseNodes.get(0) instanceof CssElIf);
    assertTrue(elseNodes.get(1) instanceof CssElse);

    verify((CssElIf) elseNodes.get(0), elifNode);

    CssElse newElseNode = (CssElse) elseNodes.get(1);
    assertEquals(1, newElseNode.getNodes().size());
    assertTrue(newElseNode.getNodes().contains(elseRule));
  }

  @Test
  public void testIfElifElifElse() {
    CssIf elifNode0 = mockCssIf(0);
    CssIf elifNode1 = mockCssIf(1);
    when(elifNode0.getElseNodes()).thenReturn(Lists.<CssNode>newArrayList(elifNode1));
    CssNode elseRule = new CssRule();
    when(elifNode1.getElseNodes()).thenReturn(Lists.newArrayList(elseRule));

    elseNodes.add(elifNode0);

    elseNodeCreator.visit(cssIf, null);

    assertEquals(3, elseNodes.size());
    assertTrue(elseNodes.get(0) instanceof CssElIf);
    assertTrue(elseNodes.get(1) instanceof CssElIf);
    assertTrue(elseNodes.get(2) instanceof CssElse);

    verify((CssElIf) elseNodes.get(0), elifNode0);
    verify((CssElIf) elseNodes.get(1), elifNode1);

    CssElse newElseNode = (CssElse) elseNodes.get(2);
    assertEquals(1, newElseNode.getNodes().size());
    assertTrue(newElseNode.getNodes().contains(elseRule));
  }

  private void verify(CssElIf toVerify, CssIf original) {
    assertEquals(3, toVerify.getNodes().size());
    for (CssNode node : original.getNodes()) {
      assertTrue(toVerify.getNodes().contains(node));
    }
    assertEquals(original.getExpression(), toVerify.getExpression());
    assertTrue(toVerify.isNegated());
    assertEquals(original.getPropertyName(), toVerify.getPropertyName());
    assertEquals(1, toVerify.getPropertyValues().length);
    assertEquals(original.getPropertyValues()[0], toVerify.getPropertyValues()[0]);
    assertEquals(0, toVerify.getElseNodes().size());
  }

  private CssIf mockCssIf(int i) {
    CssIf elifNode = mock(CssIf.class);
    when(elifNode.getNodes()).thenReturn(Lists.<CssNode>newArrayList(new CssRule(), new CssRule(),
        new CssRule()));
    when(elifNode.getExpression()).thenReturn("expression" + i);
    when(elifNode.isNegated()).thenReturn(true);
    when(elifNode.getPropertyName()).thenReturn("propertyName" + i);
    when(elifNode.getPropertyValues()).thenReturn(new String[] {"propertyValue" + i});

    return elifNode;
  }
}
