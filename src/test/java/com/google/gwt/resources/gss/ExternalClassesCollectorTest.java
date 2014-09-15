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

package com.google.gwt.resources.gss;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.css.compiler.ast.CssCompositeValueNode;
import com.google.common.css.compiler.ast.CssLiteralNode;
import com.google.common.css.compiler.ast.CssStringNode;
import com.google.common.css.compiler.ast.CssUnknownAtRuleNode;
import com.google.common.css.compiler.ast.CssValueNode;
import com.google.common.css.compiler.ast.MutatingVisitController;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;
import java.util.Set;


@RunWith(MockitoJUnitRunner.class)
public class ExternalClassesCollectorTest {
  @Mock
  private CssUnknownAtRuleNode cssUnknownAtRuleNode;
  @Mock
  private CssLiteralNode atRuleNameNode;
  @Mock
  private MutatingVisitController mutatingVisitController;
  @Mock
  private CssCompositeValueNode atRuleParameters;

  @Before
  public void setUp() {
    when(cssUnknownAtRuleNode.getName()).thenReturn(atRuleNameNode);
    when(cssUnknownAtRuleNode.getParameters()).thenReturn(
        Lists.<CssValueNode>newArrayList(atRuleParameters));
  }

  @Test
  public void leaveUnknownAtRule_notAnExternalAtRule_doNothing() {
    // Given
    ExternalClassesCollector externalClassesCollector = createAndInitExternalClassesCollector();
    when(atRuleNameNode.getValue()).thenReturn("dummy");

    // When
    externalClassesCollector.leaveUnknownAtRule(cssUnknownAtRuleNode);

    // Then
    verify(cssUnknownAtRuleNode, never()).getParameters();
    verify(mutatingVisitController, never()).removeCurrentNode();
  }

  @Test
  public void leaveUnknownAtRule_simpleExternalAtRule_classesReturnByGetExternalClass() {
    // Given
    ExternalClassesCollector externalClassesCollector = createAndInitExternalClassesCollector();
    when(atRuleNameNode.getValue()).thenReturn("external");

    List<CssValueNode> parameters = Lists.newArrayList(literalNode("externalClass"),
        literalNode("externalClass2"));
    when(atRuleParameters.getValues()).thenReturn(parameters);

    // When
    externalClassesCollector.leaveUnknownAtRule(cssUnknownAtRuleNode);

    // Then
    verify(cssUnknownAtRuleNode).getParameters();
    verify(atRuleParameters).getValues();
    verify(mutatingVisitController).removeCurrentNode();

    Set<String> externalClasses = externalClassesCollector.getExternalClassNames();
    assertEquals(2, externalClasses.size());
    assertTrue(externalClasses.contains("externalClass"));
    assertTrue(externalClasses.contains("externalClass2"));
  }


  @Test
  public void leaveUnknownAtRule_externalAtRuleWithMatchAllPrefix_allClassesAreExternals() {
    // Given
    ExternalClassesCollector externalClassesCollector =
        createAndInitExternalClassesCollector(Sets.newHashSet("class1", "class2", "class3"));
    when(atRuleNameNode.getValue()).thenReturn("external");
    List<CssValueNode> parameters = Lists.newArrayList(stringNode("*"));
    when(atRuleParameters.getValues()).thenReturn(parameters);

    // When
    externalClassesCollector.leaveUnknownAtRule(cssUnknownAtRuleNode);

    // Then
    verify(cssUnknownAtRuleNode).getParameters();
    verify(atRuleParameters).getValues();
    verify(mutatingVisitController).removeCurrentNode();

    Set<String> externalClasses = externalClassesCollector.getExternalClassNames();
    assertEquals(3, externalClasses.size());
    assertTrue(externalClasses.contains("class1"));
    assertTrue(externalClasses.contains("class2"));
    assertTrue(externalClasses.contains("class3"));
  }

  @Test
  public void leaveUnknownAtRule_externalAtRuleWithMatchAllPrefixThenAnotherExternalAtRule_anotherAtRuleNotProcessed() {
    // Given
    ExternalClassesCollector externalClassesCollector = createAndInitExternalClassesCollector();
    when(atRuleNameNode.getValue()).thenReturn("external");
    List<CssValueNode> parameters = Lists.newArrayList(stringNode("*"));
    when(atRuleParameters.getValues()).thenReturn(parameters);
    externalClassesCollector.leaveUnknownAtRule(cssUnknownAtRuleNode);
    reset(mutatingVisitController);
    CssUnknownAtRuleNode secondAtRuleNode = mock(CssUnknownAtRuleNode.class);
    CssLiteralNode secondAtRuleNameNode = mock(CssLiteralNode.class);
    when(secondAtRuleNameNode.getValue()).thenReturn("external");
    when(secondAtRuleNode.getName()).thenReturn(secondAtRuleNameNode);

    // When
    externalClassesCollector.leaveUnknownAtRule(secondAtRuleNode);

    // Then
    verify(secondAtRuleNode, never()).getParameters();
    verify(mutatingVisitController).removeCurrentNode();
  }

  @Test
  public void leaveUnknownAtRule_externalAtRuleWithPrefix_classesMatchingThePrefixAreExternals() {
    // Given
    ExternalClassesCollector externalClassesCollector =
        createAndInitExternalClassesCollector(Sets.newHashSet("prefix", "prefix-class1",
            "prefi-notexternal","external"));
    when(atRuleNameNode.getValue()).thenReturn("external");
    List<CssValueNode> parameters = Lists.newArrayList(literalNode("external"),
        stringNode("prefix*"));
    when(atRuleParameters.getValues()).thenReturn(parameters);

    // When
    externalClassesCollector.leaveUnknownAtRule(cssUnknownAtRuleNode);

    // Then
    verify(cssUnknownAtRuleNode).getParameters();
    verify(atRuleParameters).getValues();
    verify(mutatingVisitController).removeCurrentNode();

    Set<String> externalClasses = externalClassesCollector.getExternalClassNames();
    assertEquals(3, externalClasses.size());
    assertTrue(externalClasses.contains("prefix"));
    assertTrue(externalClasses.contains("prefix-class1"));
    assertTrue(externalClasses.contains("external"));
  }

  private CssValueNode literalNode(String externalClass) {
    CssValueNode node = mock(CssLiteralNode.class);
    when(node.getValue()).thenReturn(externalClass);
    return node;
  }

  private CssValueNode stringNode(String selector) {
    CssStringNode node = mock(CssStringNode.class);
    when(node.getConcreteValue()).thenReturn(selector);
    return node;
  }

  private ExternalClassesCollector createAndInitExternalClassesCollector() {
    return createAndInitExternalClassesCollector(Sets.<String>newHashSet());
  }

  private ExternalClassesCollector createAndInitExternalClassesCollector(Set<String> classNames) {
    ExternalClassesCollector externalClassesCollector =
        new ExternalClassesCollector(mutatingVisitController, classNames);

    // initialise the object but do nothing
    externalClassesCollector.runPass();

    return externalClassesCollector;
  }
}
