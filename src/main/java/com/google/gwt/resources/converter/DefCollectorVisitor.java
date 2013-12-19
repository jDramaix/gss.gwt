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

import com.google.common.base.Strings;
import com.google.gwt.resources.css.ast.Context;
import com.google.gwt.resources.css.ast.CssDef;
import com.google.gwt.resources.css.ast.CssEval;
import com.google.gwt.resources.css.ast.CssVisitor;

import java.util.HashMap;
import java.util.Map;

/**
 * GSS requires that constants are defined in upper case. This visitor will collect all existing
 * constants, create the GSS compatible name of each constant and returns a mapping of all
 * original names with the new generated name.
 */
public class DefCollectorVisitor extends CssVisitor {

  private final Map<String, String> defMapping;

  public DefCollectorVisitor() {
    this.defMapping = new HashMap<String, String>();
  }

  public Map<String, String> getDefMapping() {
    return defMapping;
  }

  @Override
  public boolean visit(CssEval x, Context ctx) {
    defMapping.put(x.getKey(), toUpperCase(x.getKey()));
    return false;
  }

  @Override
  public boolean visit(CssDef x, Context ctx) {
    defMapping.put(x.getKey(), toUpperCase(x.getKey()));
    return false;
  }

  private String toUpperCase(String camelCase) {
    if (Strings.isNullOrEmpty(camelCase) || isUpperCase(camelCase)) {
      return camelCase;
    }

    StringBuilder output = new StringBuilder().append(Character.toUpperCase(camelCase.charAt(0)));

    for (int i = 1; i < camelCase.length(); i++) {
      char c = camelCase.charAt(i);
      if (Character.isUpperCase(c)) {
        output.append('_').append(c);
      } else {
        output.append(Character.toUpperCase(c));
      }
    }

    return output.toString();
  }

  private boolean isUpperCase(String camelCase) {
    for (int i = 0; i < camelCase.length(); i++) {
      if (Character.isLowerCase(camelCase.charAt(i))) {
        return false;
      }
    }

    return true;

  }

}
