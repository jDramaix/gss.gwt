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
import com.google.gwt.resources.css.ast.Context;
import com.google.gwt.resources.css.ast.CssCompilerException;
import com.google.gwt.resources.css.ast.CssDef;
import com.google.gwt.resources.css.ast.CssEval;
import com.google.gwt.resources.css.ast.CssExternalSelectors;
import com.google.gwt.resources.css.ast.CssFontFace;
import com.google.gwt.resources.css.ast.CssIf;
import com.google.gwt.resources.css.ast.CssMediaRule;
import com.google.gwt.resources.css.ast.CssNoFlip;
import com.google.gwt.resources.css.ast.CssPageRule;
import com.google.gwt.resources.css.ast.CssProperty;
import com.google.gwt.resources.css.ast.CssProperty.ExpressionValue;
import com.google.gwt.resources.css.ast.CssProperty.IdentValue;
import com.google.gwt.resources.css.ast.CssProperty.Value;
import com.google.gwt.resources.css.ast.CssRule;
import com.google.gwt.resources.css.ast.CssSelector;
import com.google.gwt.resources.css.ast.CssSprite;
import com.google.gwt.resources.css.ast.CssUnknownAtRule;
import com.google.gwt.resources.css.ast.CssUrl;

import java.util.List;
import java.util.Map;

import static java.lang.String.format;

public class GssGenerationVisitor extends ExtendedCssVisitor {
  /* templates and tokens list */
  private static final String NO_FLIP = "/* @noflip */";
  private static final String GWT_SPRITE = "gwt-sprite: \"%s\"";
  private static final String OR = " || ";
  private static final String NOT = "!";
  private static final String IF = "@if (%s)";
  private static final String ELSE_IF = "@elseif (%s)";
  private static final String ELSE = "@else ";
  private static final String IS = "is(\"%s\", \"%s\")";
  private static final String EVAL = "eval(\"%s\")";
  private static final String DEF = "@def ";
  private static final String IMPORTANT = " !important";


  private final Map<String, String> defKeyMapping;

  private final TextOutput out;
  private boolean noFlip;
  private boolean newLine;
  private boolean needsOpenBrace;
  private boolean needsComma;

  public GssGenerationVisitor(TextOutput out, Map<String, String> defKeyMapping) {
    this.defKeyMapping = defKeyMapping;
    this.out = out;
    newLine = true;
  }

  public String getContent() {
    return out.toString();
  }

  @Override
  public void endVisit(CssFontFace x, Context ctx) {
    closeBrace();
  }


  @Override
  public void endVisit(CssMediaRule x, Context ctx) {
    out.indentOut();
    out.print("}");
    out.newlineOpt();
  }

  @Override
  public void endVisit(CssPageRule x, Context ctx) {
    out.indentOut();
    out.print("}");
    out.newlineOpt();
  }

  @Override
  public void endVisit(CssUnknownAtRule x, Context ctx) {
    out.print(x.getRule());
  }


  @Override
  public boolean visit(CssEval x, Context ctx) {
    out.print(DEF);

    String name = defKeyMapping.get(x.getKey());

    if (name == null) {
      throw new CssCompilerException("unknown @eval rule [" + x.getKey() + "]");
    }

    out.print(name);
    out.print(' ');
    out.print(format(EVAL, printValuesList(x.getValues())));

    semiColon();

    return false;
  }


  @Override
  public boolean visit(CssDef x, Context ctx) {
    out.print(DEF);

    String name = defKeyMapping.get(x.getKey());

    if (name == null) {
      throw new CssCompilerException("unknown @def rule [" + x.getKey() + "]");
    }

    out.print(name);

    out.print(' ');

    out.print(printValuesList(x.getValues()));

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

    out.print(format(GWT_SPRITE, x.getResourceFunction().getPath()));
    semiColon();

    accept(x.getProperties());

    closeBrace();
  }

  @Override
  public boolean visit(CssRule x, Context ctx) {
    if (x.getProperties().isEmpty()) {
      // Don't print empty rule blocks
      return false;
    }

    if (newLine) {
      out.newlineOpt();
    } else {
      newLine = true;
    }

    needsOpenBrace = true;
    needsComma = false;

    return true;
  }

  @Override
  public void endVisit(CssRule x, Context ctx) {
    if (!x.getProperties().isEmpty()) {
      // Don't print empty rule blocks
      closeBrace();
    }
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

    out.print(printValuesList(x.getValues().getValues()));

    if (x.isImportant()) {
      out.print(IMPORTANT);
    }

    semiColon();

    return true;
  }


  @Override
  public boolean visit(CssElse x, Context ctx) {
    closeBrace();
    out.print(ELSE);
    openBrace();
    newLine = false;

    return true;
  }

  @Override
  public boolean visit(CssElIf x, Context ctx) {
    closeBrace();
    out.print(format(ELSE_IF, printConditionnalExpression(x)));
    openBrace();
    newLine = false;

    return true;
  }

  @Override
  public void endVisit(CssIf x, Context ctx) {
      closeBrace();
      newLine = true;
  }

  @Override
  public boolean visit(CssIf x, Context ctx) {
    if (x.getExpression() != null) {
      // TODO improve warning message system
      System.out.println("Conditionnal css based on runtime evaluation is not supported yet. The " +
          "following expression is skipped: " + x);
      return false;
    }

    out.newline();
    out.print(format(IF, printConditionnalExpression(x)));
    openBrace();

    newLine = false;

    return true;
  }

  @Override
  public boolean visit(CssFontFace x, Context ctx) {
    out.print("@font-face");
    openBrace();
    return true;
  }

  @Override
  public boolean visit(CssExternalSelectors x, Context ctx) {
    // These are not valid CSS
    out.printOpt("/* @external");
    for (String className : x.getClasses()) {
      out.printOpt(" ");
      out.printOpt(className);
    }
    out.printOpt("; */");
    out.newlineOpt();
    return false;
  }

  @Override
  public boolean visit(CssMediaRule x, Context ctx) {
    out.print("@media");
    boolean isFirst = true;
    for (String m : x.getMedias()) {
      if (isFirst) {
        out.print(" ");
        isFirst = false;
      } else {
        comma();
      }
      out.print(m);
    }
    spaceOpt();
    out.print("{");
    out.newlineOpt();
    out.indentIn();
    return true;
  }

  @Override
  public boolean visit(CssPageRule x, Context ctx) {
    out.print("@page");
    if (x.getPseudoPage() != null) {
      out.print(" :");
      out.print(x.getPseudoPage());
    }
    spaceOpt();
    out.print("{");
    out.newlineOpt();
    out.indentIn();
    return true;
  }

  @Override
  public boolean visit(CssSelector x, Context ctx) {
    if (needsComma) {
      comma();
    }
    needsComma = true;
    out.print(x.getSelector());
    return true;
  }


  @Override
  public boolean visit(CssUrl x, Context ctx) {
    // These are not valid CSS
    out.printOpt("/* CssUrl */");
    out.newlineOpt();
    return false;
  }

  private void closeBrace() {
    out.indentOut();
    out.print('}');
    out.newlineOpt();
  }

  private void colon() {
    spaceOpt();
    out.print(':');
    spaceOpt();
  }

  private void comma() {
    out.print(',');
    spaceOpt();
  }

  private void openBrace() {
    spaceOpt();
    out.print('{');
    out.newlineOpt();
    out.indentIn();
  }

  private void semiColon() {
    out.print(';');
    out.newlineOpt();
  }

  private void spaceOpt() {
    out.printOpt(' ');
  }

  private String printConditionnalExpression(CssIf x) {
    if (x == null || x.getExpression() != null) {
      throw new IllegalStateException();
    }

    StringBuilder builder = new StringBuilder();

    String propertyName = x.getPropertyName();

    for (String propertyValue : x.getPropertyValues()) {
      if (builder.length() != 0) {
        builder.append(OR);
      }

      if (x.isNegated()) {
        builder.append(NOT);
      }

      builder.append(format(IS, propertyName, propertyValue));
    }

    return builder.toString();
  }

  private String printValuesList(List<Value> values) {
    StringBuilder builder = new StringBuilder();

    for (Value value : values) {
      if (value.isSpaceRequired() && builder.length() != 0) {
        builder.append(' ');
      }

      String expression = value.toCss();

      if (value instanceof IdentValue && defKeyMapping.containsKey(expression)) {
        expression = defKeyMapping.get(expression);
      } else if (value instanceof ExpressionValue) {
        expression = value.getExpression();
      }

      builder.append(expression);
    }

    return builder.toString();
  }
}
