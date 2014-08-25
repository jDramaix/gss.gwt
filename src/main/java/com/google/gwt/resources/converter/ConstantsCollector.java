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

package com.google.gwt.resources.converter;

import com.google.gwt.resources.css.ast.Context;
import com.google.gwt.resources.css.ast.CssDef;
import com.google.gwt.resources.css.ast.CssEval;
import com.google.gwt.resources.css.ast.CssUrl;
import com.google.gwt.resources.css.ast.CssVisitor;

import java.util.LinkedList;
import java.util.List;

public class ConstantsCollector extends CssVisitor {
  private final List<CssDef> constantNodes;

  public ConstantsCollector() {
    constantNodes = new LinkedList<CssDef>();
  }

  @Override
  public boolean visit(CssDef x, Context ctx) {
    constantNodes.add(x);
    return true;
  }

  @Override
  public boolean visit(CssUrl x, Context ctx) {
    constantNodes.add(x);
    return true;
  }

  @Override
  public boolean visit(CssEval x, Context ctx) {
    constantNodes.add(x);
    return true;
  }

  public List<CssDef> getConstantNodes() {
    return constantNodes;
  }
}
