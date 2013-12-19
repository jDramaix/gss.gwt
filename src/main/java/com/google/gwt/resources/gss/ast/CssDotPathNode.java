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

package com.google.gwt.resources.gss.ast;

import com.google.common.base.Strings;
import com.google.common.css.compiler.ast.CssValueNode;
import com.google.gwt.core.ext.Generator;

public class CssDotPathNode extends CssValueNode {

  public static String resolveExpression(String path, String prefix,String suffix) {
    String expression = path.replace(".", "().") + "()";

    if (!Strings.isNullOrEmpty(prefix)) {
      expression =  "\"" + Generator.escape(prefix) + "\" + " + expression;
    }

    if (!Strings.isNullOrEmpty(suffix)) {
      expression += " + \"" + Generator.escape(suffix) + "\"";
    }

    return expression;
  }

  private String suffix;
  private String prefix;
  private String path;

  public CssDotPathNode(String dotPath, String prefix, String suffix) {
    super(resolveExpression(dotPath, prefix, suffix));

    this.prefix = prefix;
    this.suffix = suffix;
    this.path = dotPath;
  }

  @Override
  public CssValueNode deepCopy() {
    return new CssDotPathNode(path, prefix, suffix);
  }

  public String getPath() {
    return path;
  }

  public String getSuffix() {
    return suffix;
  }

  public String getPrefix() {
    return prefix;
  }
}
