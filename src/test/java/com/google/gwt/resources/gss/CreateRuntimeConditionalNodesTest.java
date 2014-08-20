/*
 * Copyright 2014 Julien Dramaix.
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

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.css.compiler.ast.CssBooleanExpressionNode;
import com.google.common.css.compiler.ast.CssBooleanExpressionNode.Type;
import com.google.common.css.compiler.ast.CssConditionalRuleNode;
import com.google.common.css.compiler.ast.CssValueNode;
import com.google.common.css.compiler.ast.MutatingVisitController;
import com.google.gwt.resources.gss.ast.CssJavaExpressionNode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class CreateRuntimeConditionalNodesTest {
  private CreateRuntimeConditionalNodes visitor;

  @Before
  public void setUp() {
    MutatingVisitController controller = mock(MutatingVisitController.class);

    visitor = new CreateRuntimeConditionalNodes(controller);
  }

  @Test
  public void enterConditionalRule_noRuntimeEvaluation_childrenNotModified() {
    // Given
    CssConditionalRuleNode node = mock(CssConditionalRuleNode.class);
    when(node.numChildren()).thenReturn(3);
    when(node.getChildAt(0)).thenReturn(createChild("expression"));
    when(node.getChildAt(1)).thenReturn(createChild("is(\"locale\", \"en\")"));
    when(node.getChildAt(2)).thenReturn(createChild("evalExpression"));

    visitor.enterConditionalRule(node);

    verify(node, never()).replaceChildAt(anyInt(), anyList());
  }

  @Test
  public void enterConditionalRule_withARuntimeEvaluation_childrenModified() {
    // Given
    CssConditionalRuleNode node = mock(CssConditionalRuleNode.class);
    when(node.numChildren()).thenReturn(3);
    when(node.getChildAt(0)).thenReturn(createChild("expression"));
    when(node.getChildAt(1)).thenReturn(createChild("is(\"locale\", \"en\")"));
    when(node.getChildAt(2)).thenReturn(createChild("eval(\"expression.to.eval\")"));

    visitor.enterConditionalRule(node);

    verify(node).replaceChildAt(eq(2), argThat(isListWithCssJavaExpressionNode("expression.to" +
        ".eval")));
  }

  @Test
  public void enterConditionalRule_runtimeEvaluationUsingSimpleQuote_childrenModified() {
    // Given
    CssConditionalRuleNode node = mock(CssConditionalRuleNode.class);
    when(node.numChildren()).thenReturn(1);
    when(node.getChildAt(0)).thenReturn(createChild("eval('expression.to.eval')"));

    visitor.enterConditionalRule(node);

    verify(node).replaceChildAt(eq(0), argThat(isListWithCssJavaExpressionNode("expression.to" +
        ".eval")));
  }

  private ArgumentMatcher<List<CssValueNode>> isListWithCssJavaExpressionNode(
      final String javaExpression) {
    return new ArgumentMatcher<List<CssValueNode>>() {
      @Override
      public boolean matches(Object item) {
        List<CssValueNode> children = (List<CssValueNode>) item;
        return children != null && children.size() == 1 && children.get(0) instanceof
            CssJavaExpressionNode && javaExpression.equals(children.get(0).getValue());
      }
    };
  }

  private CssBooleanExpressionNode createChild(String expression) {
    return new CssBooleanExpressionNode(Type.CONSTANT, expression);
  }
}
