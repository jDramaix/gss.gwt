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

import com.google.common.css.compiler.ast.CssNode;
import com.google.common.css.compiler.ast.CssRootNode;
import com.google.common.css.compiler.ast.CssTree;
import com.google.common.css.compiler.ast.CssValueNode;
import com.google.common.css.compiler.passes.CompactPrinter;
import com.google.gwt.core.ext.Generator;
import com.google.gwt.resources.gss.ast.CssJavaExpressionNode;

public class CssPrinter extends CompactPrinter {

  private StringBuilder masterStringBuilder;

  public CssPrinter(CssTree tree) {
    super(tree);
  }

  public CssPrinter(CssNode node) {
    super(node);
  }

  @Override
  public boolean enterTree(CssRootNode root) {
    masterStringBuilder.append("(\"");
    return super.enterTree(root);
  }

  @Override
  public String getCompactPrintedString() {
    return masterStringBuilder.toString();
  }

  @Override
  public void leaveTree(CssRootNode root) {
    String remaining = Generator.escape(sb.toString());
    masterStringBuilder.append(remaining).append("\")");
    super.leaveTree(root);
  }

  @Override
  public void runPass() {
    masterStringBuilder = new StringBuilder();
    super.runPass();
  }

  @Override
  protected void appendValueNode(CssValueNode node) {
    if (node instanceof CssJavaExpressionNode) {
      CssJavaExpressionNode javaExpressionNode = (CssJavaExpressionNode) node;
      concat("String.valueOf(" + javaExpressionNode.getValue() + ")");
    } else {
      super.appendValueNode(node);
    }
  }

  private void concat(String stringToAppend) {
    // read, escape and reset the internal StringBuilder
    String before = Generator.escape(sb.toString());
    sb = new StringBuilder();

    masterStringBuilder.append(before);
    masterStringBuilder.append("\" + ");
    masterStringBuilder.append(stringToAppend);
    masterStringBuilder.append(" + \"");
  }
}
