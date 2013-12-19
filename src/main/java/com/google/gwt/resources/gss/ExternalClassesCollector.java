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

import com.google.common.css.compiler.ast.CssCompilerPass;
import com.google.common.css.compiler.ast.CssCompositeValueNode;
import com.google.common.css.compiler.ast.CssUnknownAtRuleNode;
import com.google.common.css.compiler.ast.CssValueNode;
import com.google.common.css.compiler.ast.DefaultTreeVisitor;
import com.google.common.css.compiler.ast.ErrorManager;
import com.google.common.css.compiler.ast.MutatingVisitController;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ExternalClassesCollector extends DefaultTreeVisitor implements CssCompilerPass {
  public static final String EXTERNAL_AT_RULE = "external";

  private final MutatingVisitController visitController;
  private final ErrorManager errorManager;

  private Set<String> externalClassNames;

  public ExternalClassesCollector(MutatingVisitController visitController,
      ErrorManager errorManager) {

    this.visitController = visitController;
    this.errorManager = errorManager;
  }

  @Override
  public void runPass() {
    this.externalClassNames = new HashSet<String>();
    visitController.startVisit(this);
  }

  @Override
  public void leaveUnknownAtRule(CssUnknownAtRuleNode node) {
    if (EXTERNAL_AT_RULE.equals(node.getName().getValue())) {
      processParameters(node.getParameters());
      visitController.removeCurrentNode();
    }
  }

  public Set<String> getExternalClassNames() {
    return externalClassNames;
  }

  private void processParameters(List<CssValueNode> values) {
    for (CssValueNode value : values) {
      if (value instanceof CssCompositeValueNode) {
        processParameters(((CssCompositeValueNode) value).getValues());
      } else {
        externalClassNames.add(value.getValue());
      }
    }
  }
}
