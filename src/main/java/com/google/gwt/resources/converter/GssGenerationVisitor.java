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

import com.google.gwt.dev.util.TextOutput;
import com.google.gwt.resources.css.CssGenerationVisitor;
import com.google.gwt.resources.css.ast.Context;
import com.google.gwt.resources.css.ast.CssCompilerException;
import com.google.gwt.resources.css.ast.CssDef;
import com.google.gwt.resources.css.ast.CssEval;
import com.google.gwt.resources.css.ast.CssNoFlip;
import com.google.gwt.resources.css.ast.CssProperty;
import com.google.gwt.resources.css.ast.CssProperty.ExpressionValue;
import com.google.gwt.resources.css.ast.CssProperty.IdentValue;
import com.google.gwt.resources.css.ast.CssProperty.Value;
import com.google.gwt.resources.css.ast.CssRule;
import com.google.gwt.resources.css.ast.CssSprite;

import java.util.List;
import java.util.Map;

public class GssGenerationVisitor extends CssGenerationVisitor {
  private static final String NO_FLIP = "/* @noflip */";
  private static final String GWT_SPRITE = "gwt-sprite";

  private final Map<String, String> defKeyMapping;

  private final TextOutput out;
  private boolean noFlip;

  public GssGenerationVisitor(TextOutput out, Map<String, String> defKeyMapping) {
    super(out, false);
    this.defKeyMapping = defKeyMapping;
    this.out = out;
  }

  public String getContent() {
    return out.toString();
  }

  @Override
  public boolean visit(CssEval x, Context ctx) {
    out.print("@def ");

    String name = defKeyMapping.get(x.getKey());

    if (name == null) {
      throw new CssCompilerException("unknown @eval rule [" + x.getKey() + "]");
    }

    out.print(name);

    out.print(" eval(\"");

    printValuesList(x.getValues());

    out.print("\")");

    semiColon();

    return false;
  }


  @Override
  public boolean visit(CssDef x, Context ctx) {
    out.print("@def ");

    String name = defKeyMapping.get(x.getKey());

    if (name == null) {
      throw new CssCompilerException("unknown @def rule [" + x.getKey() + "]");
    }

    out.print(name);

    out.print(' ');

    printValuesList(x.getValues());

    semiColon();

    return false;
  }

  @Override
  public boolean visit(CssSprite x, Context ctx) {
    out.newlineOpt();
    return false;
  }

  @Override
  public void endVisit(CssSprite x, Context ctx) {
    needsComma = false;

    accept(x.getSelectors());
    openBrace();

    out.print(GWT_SPRITE);
    out.print(": \"");
    out.print(x.getResourceFunction().getPath());
    out.print("\";");
    out.newlineOpt();

    accept(x.getProperties());

    closeBrace();
  }

  @Override
  public boolean visit(CssRule x, Context ctx) {
    out.newlineOpt();
    return super.visit(x, ctx);
  }

  @Override
  public boolean visit(CssNoFlip x, Context ctx) {
    noFlip = true;
    return true;
  }

  @Override
  public void endVisit(CssNoFlip x, Context ctx) {
    noFlip = false;
  }

  @Override
  public boolean visit(CssProperty x, Context ctx) {
    if (needsOpenBrace) {
      openBrace();
      needsOpenBrace = false;
    }

    if (noFlip) {
      out.print(NO_FLIP);
      out.print(' ');
    }

    out.print(x.getName());
    colon();

    printValuesList(x.getValues().getValues());

    if (x.isImportant()) {
      important();
    }

    semiColon();

    return true;
  }

  private void printValuesList(List<Value> values) {
    boolean first = true;
    for (Value value : values) {
      if (value.isSpaceRequired() && !first) {
        out.print(' ');
      }

      first = false;

      String expression = value.toCss();

      if (value instanceof IdentValue && defKeyMapping.containsKey(expression)) {
        expression = defKeyMapping.get(expression);
      } else if (value instanceof ExpressionValue) {
        expression = value.getExpression();
      }

      out.printOpt(expression);
    }
  }

}
