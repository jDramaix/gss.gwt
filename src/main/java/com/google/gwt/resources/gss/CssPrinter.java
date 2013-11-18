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
import com.google.gwt.resources.gss.ast.CssDotPathNode;
import com.google.gwt.resources.gss.ast.CssJavaExpressionNode;

public class CssPrinter extends CompactPrinter {
  /**
   * This value is used by {@link #concat} to help create a more balanced AST
   * tree by producing parenthetical expressions.
   */
  private static final int CONCAT_EXPRESSION_LIMIT = 20;

  private StringBuilder masterStringBuilder;
  private String css;
  private int concatenationNumber;

  public CssPrinter(CssTree tree) {
    super(tree);
  }

  public CssPrinter(CssNode node) {
    super(node);
  }

  @Override
  public boolean enterTree(CssRootNode root) {
    masterStringBuilder.append("(");
    return super.enterTree(root);
  }

  @Override
  public String getCompactPrintedString() {
    return css;
  }

  @Override
  public void leaveTree(CssRootNode root) {
    masterStringBuilder.append(flushInternalStringBuilder()).append(")");
    super.leaveTree(root);
  }

  @Override
  public void runPass() {
    masterStringBuilder = new StringBuilder();
    concatenationNumber = 0;

    super.runPass();

    css = masterStringBuilder.toString();
  }

  @Override
  protected void appendValueNode(CssValueNode node) {
    if (node instanceof CssJavaExpressionNode || node instanceof CssDotPathNode) {
      concat(node.getValue());
    } else {
      super.appendValueNode(node);
    }
  }

  private void concat(String stringToAppend) {
    masterStringBuilder.append(flushInternalStringBuilder());

    appendConcatOperation();

    masterStringBuilder.append(stringToAppend);

    appendConcatOperation();
  }

  private void appendConcatOperation() {
    // avoid long string concatenation chain
    if (concatenationNumber >= CONCAT_EXPRESSION_LIMIT) {
      masterStringBuilder.append(") + (");
      concatenationNumber = 0;
    } else {
      masterStringBuilder.append(" + ");
      concatenationNumber++;
    }
  }

  /**
   * Read what the internal StringBuilder used by the CompactPrinter has already built. Escape it.
   * and reset the internal StringBuilder
   * @return
   */
  private String flushInternalStringBuilder() {
    String content = "\"" + Generator.escape(sb.toString()) + "\"";
    sb = new StringBuilder();

    return content;
  }
}
