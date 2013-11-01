/*
 * Copyright 2013 GWT project.
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

import com.google.common.collect.Lists;
import com.google.common.css.compiler.ast.CssConstantReferenceNode;
import com.google.common.css.compiler.ast.CssDefinitionNode;
import com.google.common.css.compiler.ast.CssLiteralNode;
import com.google.common.css.compiler.ast.CssNode;
import com.google.common.css.compiler.ast.CssUnknownAtRuleNode;
import com.google.common.css.compiler.ast.CssValueNode;
import com.google.common.css.compiler.ast.ErrorManager;
import com.google.common.css.compiler.ast.GssError;
import com.google.common.css.compiler.ast.MutatingVisitController;
import com.google.common.css.compiler.passes.CreateDefinitionNodes;
import com.google.gwt.resources.gss.ast.CssJavaExpressionNode;

import java.util.List;

/**
 * A compiler pass that transforms each well-formed {@code @def}
 * {@link com.google.common.css.compiler.ast.CssUnknownAtRuleNode} to a
 * {@link com.google.common.css.compiler.ast.CssDefinitionNode} and each well-formed {@code @eval}
 * {@link com.google.common.css.compiler.ast.CssUnknownAtRuleNode} to a
 * {@link com.google.gwt.resources.gss.ast.CssJavaExpressionNode}
 */
public class CreateEvalAndDefinitionNodes extends CreateDefinitionNodes {
  private static final String evalName = "eval";
  private final MutatingVisitController visitController;
  private final ErrorManager errorManager;

  public CreateEvalAndDefinitionNodes(MutatingVisitController visitController,
      ErrorManager errorManager) {
    super(visitController, errorManager);
    this.visitController = visitController;
    this.errorManager = errorManager;
  }

  @Override
  public boolean enterUnknownAtRule(CssUnknownAtRuleNode node) {
    if (node.getName().getValue().equals(evalName)) {
      if (node.getType().hasBlock()) {
        reportError("@" + evalName + " cannot have block", node);
        return false;
      }

      List<CssValueNode> params = node.getParameters();
      if (params.size() != 2) {
        reportError("Incorrect number of parts for @" + evalName, node);
        return false;
      }

      CssNode nameNode = params.get(0);
      if (!(nameNode instanceof CssLiteralNode)) {
        reportError("@" + evalName + " without a valid literal as name", node);
        return false;
      }

      CssLiteralNode evalNameNode = (CssLiteralNode) nameNode;
      String evalName = evalNameNode.getValue();
      if (!CssConstantReferenceNode.isDefinitionReference(evalName)) {
        errorManager.reportWarning(new GssError(String.format(
            "WARNING for invalid @"+ evalName+ "name %s (should be defined in uppercase). We " +
            "ignore this.", evalName), evalNameNode.getSourceCodeLocation()));
      }

      CssJavaExpressionNode expressionNode = new CssJavaExpressionNode(params.get(1).getValue());

      CssDefinitionNode def = new CssDefinitionNode(
          Lists.newArrayList((CssValueNode) expressionNode),
          (CssLiteralNode) nameNode,
          node.getComments(),
          node.getSourceCodeLocation());

      visitController.replaceCurrentBlockChildWith(Lists.newArrayList((CssNode) def), false);

      return false;
    } else {
      // will handle @def at-rule
      return super.enterUnknownAtRule(node);
    }
  }

  private void reportError(String message, CssNode node) {
    errorManager.report(new GssError(message, node.getSourceCodeLocation()));
    visitController.removeCurrentNode();
  }
}
