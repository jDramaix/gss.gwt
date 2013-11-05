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

import com.google.common.collect.ImmutableList;
import com.google.common.css.compiler.ast.CssValueNode;
import com.google.common.css.compiler.ast.ErrorManager;
import com.google.common.css.compiler.ast.GssFunction;
import com.google.common.css.compiler.ast.GssFunctionException;
import com.google.gwt.resources.gss.ast.CssDotPathNode;

import java.util.List;

public class ValueFunction implements GssFunction {
  public static String getName() {
    return "value";
  }

  @Override
  public List<CssValueNode> getCallResultNodes(List<CssValueNode> args, ErrorManager errorManager)
      throws GssFunctionException {
    if (args.size() == 0 || args.size() > 2) {
      throw new GssFunctionException(getName() + " function take one or two arguments");
    }

    String functionPath = args.get(0).getValue();
    String suffix = null;

    if (args.size() == 2) {
      suffix = args.get(1).getValue();
    }

    CssDotPathNode cssDotPathNode = new CssDotPathNode(functionPath, suffix);

    // TODO add validation : maybe add a compilation pass that will validate the the method exist
    // on the resource bundle

    return ImmutableList.of((CssValueNode) cssDotPathNode);
  }

  @Override
  public String getCallResultString(List<String> args) throws GssFunctionException {

    String functionPath = args.get(0);
    String suffix = null;

    if (args.size() == 2) {
      suffix = args.get(1);
    }
    return CssDotPathNode.resolveExpression(functionPath, suffix);
  }

  @Override
  public Integer getNumExpectedArguments() {
    // number of arguments is variable
    return null;
  }
}

